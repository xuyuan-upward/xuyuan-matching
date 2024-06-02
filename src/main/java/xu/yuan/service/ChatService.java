package xu.yuan.service;

import xu.yuan.model.domain.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.ChatRequest;
import xu.yuan.model.vo.ChatMessageVO;
import xu.yuan.model.vo.PrivateChatVO;

import java.util.Date;
import java.util.List;

/**
* @author 肖广龙
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2024-05-30 21:12:05
*/
public interface ChatService extends IService<Chat> {
    /**
     * 删除密钥
     *
     * @param cacheChatTeam 前缀密钥
     * @param valueOf id
     */
    void deleteKey(String cacheChatTeam, String valueOf);

    ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);

    List<ChatMessageVO> getCache(String redisKey, String id);

    /**
     * 获取大厅聊天记录
     * @param hallChat
     * @param loginUser
     * @return
     */
    List<ChatMessageVO> getHallChat(int hallChat, User loginUser);

    void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVOS);

    /**
     * 获取队伍聊天信息
     * @param teamChat
     * @param loginUser
     * @param chatRequest
     * @return
     */
    List<ChatMessageVO> getTeamChat(int teamChat, User loginUser, ChatRequest chatRequest);

    /**
     * 返回私人聊天信息表
     * @param id
     * @return
     */
    List<PrivateChatVO> getPrivateList(long id);

    /**
     * 返回私人聊天信息记录
     * @param chatRequest
     * @param privateChat
     * @param loginUser
     * @return
     */
    List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int privateChat, User loginUser);
}
