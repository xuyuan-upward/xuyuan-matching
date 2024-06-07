package xu.yuan.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javafx.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.Chat;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.ChatRequest;
import xu.yuan.model.vo.ChatMessageVO;
import xu.yuan.model.vo.PrivateChatVO;
import xu.yuan.model.vo.WebSocketVO;
import xu.yuan.service.ChatService;
import xu.yuan.mapper.ChatMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.TeamService;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static xu.yuan.Constant.ChatConstant.*;
import static xu.yuan.Constant.RedisConstants.*;
import static xu.yuan.Constant.UserConstant.ADMIN_ROLE;

/**
* @author 肖广龙
* @description 针对表【chat(聊天消息表)】的数据库操作Service实现
* @createDate 2024-05-30 21:12:05
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService{

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TeamService teamService;
    /**
     * 从新发送数据时候，删除脏数据
     * @param redisKey
     * @param id
     */
    @Override
    public void deleteKey(String redisKey, String id) {
        try {
            // 代表缓存大厅的数据
            if (redisKey.equals(CACHE_CHAT_HALL)) {
                redisTemplate.delete(redisKey);
            } else {
                //可能还需要缓存其他大厅用户的单独数据
                redisTemplate.delete(redisKey + id);
            }
        } catch (Exception e) {
            log.error("redis delete key Error");
        }
    }

    /**
     * 聊天结果
     *
     * @param userId     发送方
     * @param toId       接收方
     * @param text       文本
     * @param chatType   聊天类型
     * @param createTime 创建时间
     * @return {@link ChatMessageVO}
     */
    @Override
    public ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.getById(userId);
        User toUser = userService.getById(toId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
        WebSocketVO toWebSocketVo = new WebSocketVO();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        BeanUtils.copyProperties(toUser, toWebSocketVo);
        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setToUser(toWebSocketVo);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setText(text);
        chatMessageVo.setCreateTime(DateUtil.format(createTime, "yyyy-MM-dd HH:mm:ss"));
        return chatMessageVo;
    }

    /**
     * 获取缓存
     *
     * @param redisKey redis键
     * @param id       id
     * @return {@link List}<{@link ChatMessageVO}>
     */
    //    getCache => 如何缓存的不是公共数据需要加上reis键+和标识某用户或者队伍的id
    @Override
    public List<ChatMessageVO> getCache(String redisKey, String id) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        List<ChatMessageVO> chatRecords;
        if (redisKey.equals(CACHE_CHAT_HALL)) {
            chatRecords = (List<ChatMessageVO>) valueOperations.get(redisKey);
        } else {
            chatRecords = (List<ChatMessageVO>) valueOperations.get(redisKey + id);
        }
        return chatRecords;
    }

    /**
     * 获取大厅的聊天信息
     * @param chatType 聊天类型
     * @param loginUser 当前登录的用户
     * @return
     */

    @Override
    public List<ChatMessageVO> getHallChat(int chatType, User loginUser) {
        List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()));
        // 缓存有数据 直接取出来
        if (chatRecords != null) {
            List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
            // 重新更新存放缓存数据
            saveCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
            return chatMessageVOS;
        } // 从数据库里面获取
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, chatType);
        List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, null, chatLambdaQueryWrapper);
        saveCache(CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVOS);
        return chatMessageVOS;
    }
    /**
     * 聊天结果
     *
     * @param userId 用户id
     * @param text   文本
     * @return {@link ChatMessageVO}
     */
    private ChatMessageVO chatResult(Long userId, String text) {
        ChatMessageVO chatMessageVo = new ChatMessageVO();
        User fromUser = userService.getById(userId);
        WebSocketVO fromWebSocketVo = new WebSocketVO();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        chatMessageVo.setFromUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        return chatMessageVo;
    }
    /**
     * 设置是否属于自己的信息
     *
     * @param loginUser              登录用户
     * @param userId                 用户id
     * @param chatLambdaQueryWrapper 聊天lambda查询包装器
     * @return {@link List}<{@link ChatMessageVO}>
     */
    private List<ChatMessageVO> returnMessage(User loginUser,
                                              Long userId,
                                              LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        return chatList.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(chat.getFromId(), chat.getText());
            boolean isCaptain = userId != null && userId.equals(chat.getFromId());
            if (userService.getById(chat.getFromId()).getRole() == ADMIN_ROLE || isCaptain) {
                chatMessageVo.setIsAdmin(true);
            }
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            // TODO  DateUtil.format(chat.getCreateTime()来自hutool工具时间转化格式 => 根据时间转化为对应的格式
            chatMessageVo.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyy-MM-dd HH:mm:ss"));
            return chatMessageVo;
        }).collect(Collectors.toList());
    }
    /**
     * 获取用户点赞消息数量
     *
     * @param request 请求
     * @return
     */
    @GetMapping("/like/num")
    @ApiOperation(value = "获取用户点赞消息数量")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<Long> getUserLikeMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        long messageNum = 1 ;/*messageService.getLikeNum(loginUser.getId())*/
        return ResultUtils.success(messageNum);
    }
    /**
     * 判断是否是我发的消息信息
     *
     * @param loginUser   登录用户
     * @param chatRecords 聊天记录
     * @return {@link List}<{@link ChatMessageVO}>
     */
//
//    使用peek来进行状态修改并不是最佳实践，
//    因为它的主要目的是为了观察流中的元素，
//    而非修改。在某些情况下，使用map可能更合适，
//    但如果修改操作不改变元素的类型或者不需要映射到新流类型，
//    且主要是为了副作用（side-effect），那么peek就是可行的选择。

    private List<ChatMessageVO> checkIsMyMessage(User loginUser, List<ChatMessageVO> chatRecords) {
        return chatRecords.stream().peek(chat -> {
            // 表示聊天发送方不是我，且当前聊天记录是属于他自己的，此时就代表这条信息不是属于我的 => 即为当前用户的
            if (chat.getFromUser().getId() != loginUser.getId() && chat.getIsMy()) {
                chat.setIsMy(false);
            }
            if (chat.getFromUser().getId() == loginUser.getId() && !chat.getIsMy()) {
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 保存缓存
     *
     * @param redisKey       redis键
     * @param id             id
     * @param chatMessageVOS 聊天消息vos
     */
    //    saveCache为什么多设置一个id呢 => 如何缓存的不
    //    是公共数据需要加上reis键+和标识某用户或者队伍的id
    @Override
    public void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVOS) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            // 解决缓存雪崩 设置随机过期时间
            int i = RandomUtil.randomInt(MINIMUM_CACHE_RANDOM_TIME, MAXIMUM_CACHE_RANDOM_TIME);
            // 代表缓存大厅的数据
            if (redisKey.equals(CACHE_CHAT_HALL)) {
                valueOperations.set(
                        redisKey,
                        chatMessageVOS,
                        MINIMUM_CACHE_RANDOM_TIME + i / CACHE_TIME_OFFSET,
                        TimeUnit.MINUTES);
            } else {
                //可能还需要缓存其他大厅用户的单独数据
                valueOperations.set(
                        redisKey + id,
                        chatMessageVOS,
                        MINIMUM_CACHE_RANDOM_TIME + i / CACHE_TIME_OFFSET,
                        TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key Error");
        }
    }

    @Override
    public List<ChatMessageVO> getTeamChat(int teamChat, User loginUser, ChatRequest chatRequest) {
        Long teamId = chatRequest.getTeamId();
        if (teamId == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR,"队伍错误");
        }
        // 获取缓存数据
        List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_TEAM, String.valueOf(teamId));
        if (chatRecords != null) {
            List<ChatMessageVO> chatMessageVOS = checkIsMyMessage(loginUser, chatRecords);
            saveCache(CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
            return chatMessageVOS;
        }
        Team team = teamService.getById(teamId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, teamChat).eq(Chat::getTeamId, teamId);
        List<ChatMessageVO> chatMessageVOS = returnMessage(loginUser, team.getUserId(), chatLambdaQueryWrapper);
        // 由于第一次没有数据，获取到的数据存放到redis中去
        saveCache(CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVOS);
        return chatMessageVOS;
    }

    /**
     * 返回私人聊天信息列表
     * @param id
     * @return
     */
    @Override
    public List<PrivateChatVO> getPrivateList(long id) {
        if (id < 1) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Chat> Chatwrapper = new LambdaQueryWrapper<>();
        Chatwrapper.eq(Chat::getFromId, id).eq(Chat::getChatType, PRIVATE_CHAT);
        List<Chat> MySend = this.list(Chatwrapper);
        // 去除重复的元素
        HashSet<Long> userIdSet = new HashSet<>();
        MySend.forEach((chat) -> {
            // 获取我发送的人的id
            Long toId = chat.getToId();
            userIdSet.add(toId);
        });
        // 把wrapper的条件清除
        Chatwrapper.clear();
        Chatwrapper.eq(Chat::getToId, id).eq(Chat::getChatType, PRIVATE_CHAT);
        // 获取我接收的信息
        List<Chat> myReceive = this.list(Chatwrapper);
        myReceive.forEach((chat) -> {
            //获取我接收消息的人id
            Long fromId = chat.getFromId();
            userIdSet.add(fromId);
        });
        // 统一得出关于我和其他人联系的所有信息
        List<User> userList = userService.listByIds(userIdSet);
        List<PrivateChatVO> privateChatVOS = userList.stream().map((user) -> {
            PrivateChatVO privateChatVO = new PrivateChatVO();
            privateChatVO.setUserId(user.getId());
            privateChatVO.setUsername(user.getUsername());
            privateChatVO.setAvatarUrl(user.getAvatarUrl());
            Pair<String, Date> pair = getPrivateLastMessage(Long.valueOf(id), user.getId());
            privateChatVO.setLastMessage(pair.getKey());
            privateChatVO.setLastMessageDate(DateUtil.format(pair.getValue(), "yyyy-MM-dd HH:mm:ss"));
            // 获取当前未读消息pair.getValue()
//            privateChatVO.setUnReadNum(getUnreadNum(, user.getId()));
            return privateChatVO;
        }).sorted().collect(Collectors.toList());
        return privateChatVOS;
    }

    /**
     * 返回私人聊天记录
     * @param chatRequest
     * @param chatType
     * @param loginUser
     * @return
     */
    @Override
    public List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser) {
        // 获取发送方id
        Long toId = chatRequest.getToId();
        if (toId == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        // 获取缓存内的数据 尽量减少缓存数据的不同步时间
        List<ChatMessageVO> chatRecords = getCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId));
        if (chatRecords != null) {
            // 用来保存缓存消息的同时实现判断是否是自己的消息
            saveCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId), chatRecords);
            return chatRecords;
        }
        // 缓存内没有数据时候直接从数据库取
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.
         // sql => where ((getFromId = loginUser.getId() and getToId = toId ) or
         //(getFromId = toId and getToId = loginUser.getId()))  这两行都是属于一个括号内的内容
         //and  getChatType = chatType
                and(privateChat -> privateChat.eq(Chat::getFromId, loginUser.getId()).eq(Chat::getToId, toId)
                        .or().
                                eq(Chat::getToId, loginUser.getId()).eq(Chat::getFromId, toId)
                ).eq(Chat::getChatType, chatType);
        // 两方共有聊天 获取有关这些我的所有信息
        List<Chat> list = this.list(chatLambdaQueryWrapper);
        List<ChatMessageVO> chatMessageVOList = list.stream().map(chat -> {
            ChatMessageVO chatMessageVo = chatResult(loginUser.getId(),
                    toId, chat.getText(), chatType,
                    chat.getCreateTime());
            //判断是否是当前用户所发
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            return chatMessageVo;
        }).collect(Collectors.toList());
        saveCache(CACHE_CHAT_PRIVATE, loginUser.getId() + String.valueOf(toId), chatMessageVOList);
        return chatMessageVOList;
    }

    /**
 * 获取私聊最后一条消息信息
 *
 * @param loginId  登录id
 * @param remoteId 遥远id
 * @return {@link String}
 */
        private Pair<String, Date> getPrivateLastMessage(Long loginId, Long remoteId) {
            LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 获取我发的
            chatLambdaQueryWrapper.eq(Chat::getFromId, loginId).eq(Chat::getToId, remoteId).eq(Chat::getChatType, PRIVATE_CHAT)
                    .orderBy(true, false, Chat::getCreateTime);
            List<Chat> chatList1 = this.list(chatLambdaQueryWrapper);
            chatLambdaQueryWrapper.clear();
            // 获取我接收的
            chatLambdaQueryWrapper.eq(Chat::getFromId, remoteId).eq(Chat::getToId, loginId).eq(Chat::getChatType, PRIVATE_CHAT)
                    .orderBy(true, false, Chat::getCreateTime);
            List<Chat> chatList2 = this.list(chatLambdaQueryWrapper);
            if (chatList1.isEmpty() && chatList2.isEmpty()) {
                return new Pair<>("", null);
            }
            if (chatList1.isEmpty()) {
                return new Pair<>(chatList2.get(0).getText(), chatList2.get(0).getCreateTime());
            }
            if (chatList2.isEmpty()) {
                return new Pair<>(chatList1.get(0).getText(), chatList1.get(0).getCreateTime());
            }
            // 两个都有信息
            // 代表chatList1是最新消息
            if (chatList1.get(0).getCreateTime().after(chatList2.get(0).getCreateTime())) {
                return new Pair<>(chatList1.get(0).getText(), chatList1.get(0).getCreateTime());
            }
            // 代表chatList2是最新消息
            else {
                return new Pair<>(chatList2.get(0).getText(), chatList2.get(0).getCreateTime());
            }
        }
}




