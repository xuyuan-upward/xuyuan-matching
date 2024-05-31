package xu.yuan.websocekt;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import xu.yuan.config.HttpSessionConfig;
import xu.yuan.model.domain.Chat;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.MessageRequest;
import xu.yuan.model.vo.ChatMessageVO;
import xu.yuan.model.vo.WebSocketVO;
import xu.yuan.service.ChatService;
import xu.yuan.service.TeamService;
import xu.yuan.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static xu.yuan.Constant.ChatConstant.*;
import static xu.yuan.Constant.UserConstant.ADMIN_ROLE;
import static xu.yuan.Constant.UserConstant.USER_LOGIN_STATE;


/**
 * WebSocket聊天室
 *
 * @author xuyuan
 * @date 2023/06/22
 */
@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{userId}/{teamId}", configurator = HttpSessionConfig.class)
public class WebSocket {
    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, WebSocket>> ROOMS = new HashMap<>();

    /**
     * 线程安全的无序的集合
     */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * 会话池
     */
    private static final Map<String, Session> SESSION_POOL = new HashMap<>(0);
    /**
     * 用户服务
     */
    private static UserService userService;
    /**
     * 聊天服务
     */
    private static ChatService chatService;
    /**
     * 团队服务
     */
    private static TeamService teamService;

    /**
     * 房间在线人数
     */
    private static int onlineCount = 0;

    /**
     * 当前信息
     */
    private Session session;

    /**
     * http会话
     */
    private HttpSession httpSession;


    /**
     * 上网数
     *
     * @return int
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 添加在线计数
     */
    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    /**
     * 子在线计数
     */
    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    /**
     * 集热地图服务
     *
     * @param userService 用户服务
     */
    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocket.userService = userService;
    }

    /**
     * 集热地图服务
     *
     * @param chatService 聊天服务
     */
    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocket.chatService = chatService;
    }

    /**
     * 集热地图服务
     *
     * @param teamService 团队服务
     */
    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocket.teamService = teamService;
    }


    /**
     * 队伍内群发消息
     *
     * @param teamId 团队id
     * @param msg    消息
     */
    public static void broadcast(String teamId, String msg) {
        ConcurrentHashMap<String, WebSocket> map = ROOMS.get(teamId);
        // keySet获取map集合key的集合  然后在遍历key即可
        for (String key : map.keySet()) {
            try {
                WebSocket webSocket = map.get(key);
                // 每个webSocket对象都有属于自己的session对话，此时就是将整个队伍的的用户都发了一个遍
                webSocket.sendMessage(msg);
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @throws IOException ioexception
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 建立 连接，调用
     *
     * @param session 会话
     * @param userId  用户id
     * @param teamId  团队id
     * @param config  配置
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "userId") String userId,
                       @PathParam(value = "teamId") String teamId,
                       EndpointConfig config) {
        try {
            //判断是否是空
            if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
                sendError(userId, "参数有误");
                return;
            }
            HttpSession userHttpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            //根据当前用户HttpSession获取当前登录用户
            User user = (User) userHttpSession.getAttribute(USER_LOGIN_STATE);
            if (user != null) {
                this.session = session;
                this.httpSession = userHttpSession;
            }
            if (!"NaN".equals(teamId)) {
                if (!ROOMS.containsKey(teamId)) {
                    ConcurrentHashMap<String, WebSocket> room = new ConcurrentHashMap<>(0);
                    room.put(userId, this);
                    ROOMS.put(String.valueOf(teamId), room);
                    // 在线数加1
                    addOnlineCount();
                } else {
                    if (!ROOMS.get(teamId).containsKey(userId)) {
                        ROOMS.get(teamId).put(userId, this);
                        // 在线数加1
                        addOnlineCount();
                    }
                }
            } else {
                SESSIONS.add(session);
                SESSION_POOL.put(userId, session);
                // 给所有用户传播一个自己已经上线的信息
                sendAllUsers();
            }
        } catch (Exception e) {
            log.error("exception message", e);
        }
    }

    /**
     * 关闭
     *
     * @param userId  用户id
     * @param teamId  团队id
     * @param session 会话
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId,
                        @PathParam(value = "teamId") String teamId,
                        Session session) {
        try {
            if (!"NaN".equals(teamId)) {
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0) {
                    subOnlineCount();
                }
            } else {
                if (!SESSION_POOL.isEmpty()) {
                    SESSION_POOL.remove(userId);
                    //将该会话移除
                    SESSIONS.remove(session);
                }
                sendAllUsers();
            }
        } catch (Exception e) {
            log.error("exception message", e);
        }
    }

    /**
     * 消息
     *
     * @param message 消息
     * @param userId  用户id
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        //心跳包，隔一段时间前端调用发送方法给服务端，维持该会话
//        1、websocket连接后，长时间远端和客户端不发消息，服务端会把websocket给断开；
//        2、所以就需要一种机制来检测客户端和服务端是否处于正常的链接状态。
//        3、因此就有了websocket的‘心跳监测’。
//        4、还有心跳，说明还活着，没有心跳说明已经断开了
//        由前端发送
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            return;
        }
        // 解析发送的信息 => 解析 接收方 队伍 聊天类型 内容文本 是否为管理员
        // fromJson将对应json数据字符串 转换成指定的Java对象类型
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        // 接收方id
        Long toId = messageRequest.getToId();
        // 哪个队伍
        Long teamId = messageRequest.getTeamId();
        // 文本
        String text = messageRequest.getText();
        // 聊天类型
        Integer chatType = messageRequest.getChatType();
        // 获取当前发送用户
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(teamId);
        if (chatType == PRIVATE_CHAT) {
            // 私聊
            privateChat(fromUser, toId, text, chatType);
        } else if (chatType == TEAM_CHAT) {
            // 队伍内聊天
            teamChat(fromUser, text, team, chatType);
        } else {
            // 群聊
            hallChat(fromUser, text, chatType);
        }
    }

    /**
     * 队伍聊天
     *
     * @param user     发送信息的用户
     * @param text     聊天内容文本
     * @param team     发到的团队
     * @param chatType 聊天类型
     */
    private void teamChat(User user, String text, Team team, Integer chatType) {
        // 创建返回前端的数据VO对象
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        // 创建返回前端用来展示当前用户的数据VO对象
        WebSocketVO fromWebSocketVO = new WebSocketVO();
        BeanUtils.copyProperties(user, fromWebSocketVO);
        //发送用户
        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setTeamId(team.getId());
        chatMessageVo.setChatType(chatType);
        //发送时间
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        // 是否是管理员
        if (Objects.equals(user.getId(), team.getUserId()) || user.getRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
        // 获取当前登录用户 并判断是否是自己发的信息
        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = new Gson().toJson(chatMessageVo);
        try {
            //广播该用户消息到整个队伍当中
            broadcast(String.valueOf(team.getId()), toJson);
            // 保存当前信息
            saveChat(user.getId(), null, text, team.getId(), chatType);
            // 删除当前缓存队伍聊天信息 ，防止造成数据更新，读缓存，读到脏数据
            chatService.deleteKey(CACHE_CHAT_TEAM, String.valueOf(team.getId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 大厅聊天
     *
     * @param user     用户
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void hallChat(User user, String text, Integer chatType) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        WebSocketVO fromWebSocketVO = new WebSocketVO();
        BeanUtils.copyProperties(user, fromWebSocketVO);
        chatMessageVo.setFromUser(fromWebSocketVO);
        chatMessageVo.setText(text);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setCreateTime(DateUtil.format(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
        if (user.getRole() == ADMIN_ROLE) {
            chatMessageVo.setIsAdmin(true);
        }
        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = new Gson().toJson(chatMessageVo);
        sendAllMessage(toJson);
        saveChat(user.getId(), null, text, null, chatType);
        chatService.deleteKey(CACHE_CHAT_HALL, String.valueOf(user.getId()));
    }

    /**
     * 私聊
     *
     * @param user     发送的用户 注意，不是代表的是当前用户发的！！！
     * @param toId     为私聊的用户id
     * @param text     文本
     * @param chatType 聊天类型
     */
    private void privateChat(User user, Long toId, String text, Integer chatType) {
        // 获取发送的消息
        ChatMessageVO chatMessageVo = chatService
                .chatResult(user.getId(), toId, text, chatType, DateUtil.date(System.currentTimeMillis()));
        User loginUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
        if (Objects.equals(loginUser.getId(), user.getId())) {
            chatMessageVo.setIsMy(true);
        }
        String toJson = new Gson().toJson(chatMessageVo);
        sendOneMessage(toId.toString(), toJson);
        saveChat(user.getId(), toId, text, null, chatType);
        chatService.deleteKey(CACHE_CHAT_PRIVATE, user.getId() + "" + toId);
        chatService.deleteKey(CACHE_CHAT_PRIVATE, toId + "" + user.getId());
    }

    /**
     * 保存聊天
     *
     * @param userId   用户id
     * @param toId     私聊接收方的id
     * @param text     文本
     * @param teamId   团队id
     * @param chatType 聊天类型
     */
    private void saveChat(Long userId, Long toId, String text, Long teamId, Integer chatType) {
//        if (chatType == PRIVATE_CHAT) {
//            User user = userService.getById(userId);
//            Set<Long> userIds = stringJsonListToLongSet(user.getFriendIds());
//            if (!userIds.contains(toId)) {
//                sendError(String.valueOf(userId), "该用户不是你的好友");
//                return;
//            }
//        }
        Chat chat = new Chat();
        chat.setFromId(userId);
        chat.setText(String.valueOf(text));
        chat.setChatType(chatType);
        chat.setCreateTime(new Date());
        if (toId != null && toId > 0) {
            chat.setToId(toId);
        }
        if (teamId != null && teamId > 0) {
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }

    /**
     * 发送失败
     *
     * @param userId       用户id
     * @param errorMessage 错误消息
     */
    private void sendError(String userId, String errorMessage) {
        JSONObject obj = new JSONObject();
        obj.set("error", errorMessage);
        sendOneMessage(userId, obj.toString());
    }

    /**
     * 广播消息
     *
     * @param message 消息
     */
    public void sendAllMessage(String message) {
        for (Session userSession : SESSIONS) {
            try {
                if (userSession.isOpen()) {
                    synchronized (userSession) {
                        userSession.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }


    /**
     * 发消息送到对应的一个用户  这属于私发了
     *
     * @param userId  用户编号
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        Session userSession = SESSION_POOL.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                synchronized (userSession) {
                    userSession.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("exception message", e);
            }
        }
    }

    /**
     *  创建所有客户端，并进行发送给所有的
     */
    public void sendAllUsers() {
        HashMap<String, List<WebSocketVO>> stringListHashMap = new HashMap<>(0);
        List<WebSocketVO> webSocketVos = new ArrayList<>();
        stringListHashMap.put("users", webSocketVos);
        for (Serializable key : SESSION_POOL.keySet()) {
            User user = userService.getById(key);
            WebSocketVO webSocketVO = new WebSocketVO();
            BeanUtils.copyProperties(user, webSocketVO);
            webSocketVos.add(webSocketVO);
        }
        // 将对应的每个用户的基本展示信息 转换成对应的json数据
//        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap));
        sendAllMessage(new Gson().toJson(stringListHashMap));
    }
}
