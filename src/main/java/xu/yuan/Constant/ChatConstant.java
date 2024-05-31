package xu.yuan.Constant;


/**
 * 聊天常量
 *
 * @author xuyuan
 * @date 2023/06/22
 */
public final class ChatConstant {
    private ChatConstant() {
    }

    /**
     * 私聊
     */
    public static final int PRIVATE_CHAT = 1;

    /**
     * 队伍群聊
     */

    public static final int TEAM_CHAT = 2;
    /**
     * 大厅聊天
     */
    public static final int HALL_CHAT = 3;

    /**
     * 缓存聊天大厅信息
     */
    public static final String CACHE_CHAT_HALL = "xuyuan:chat:chat_records:chat_hall";

    /**
     * 缓存私人聊天信息
     */
    public static final String CACHE_CHAT_PRIVATE = "xuyuan:chat:chat_records:chat_private:";

    /**
     * 缓存聊天团队信息
     */
    public static final String CACHE_CHAT_TEAM = "xuyuan:chat:chat_records:chat_team:";
}
