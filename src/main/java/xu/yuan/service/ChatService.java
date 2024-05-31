package xu.yuan.service;

import xu.yuan.model.domain.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.vo.ChatMessageVO;

import java.util.Date;

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
}
