package xu.yuan.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import xu.yuan.model.domain.Chat;
import xu.yuan.model.domain.User;
import xu.yuan.model.vo.ChatMessageVO;
import xu.yuan.model.vo.WebSocketVO;
import xu.yuan.service.ChatService;
import xu.yuan.mapper.ChatMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import java.util.Date;

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


    /**
     * 删除脏数据
     * @param cacheChatTeam
     * @param valueOf
     */
    @Override
    public void deleteKey(String cacheChatTeam, String valueOf) {

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
}




