package xu.yuan.Constant;

/**
 * Redis常量
 *
 * @author xuyuan
 */
public final class RedisConstants {
    private RedisConstants() {
    }
    public static final String TEAM_LOCK_KEY = "team_lock";
    public static final String USER_LOCK_KEY = "user_lock";
    public static final int MAX_USER_TEAM_COUNT = 5;
    public static final String LOGIN_USER_KEY = "xuyuan:login:token:";

    public static final Long LOGIN_USER_TTL = 15L;
    /**
     * 注册验证码键
     */
    public static final String REGISTER_CODE_KEY = "xuyuan:register:";
    /**
     * 注册验证码过期时间
     */
    public static final Long REGISTER_CODE_TTL = 15L;
    /**
     * 用户更新电话键
     */
    public static final String USER_UPDATE_PHONE_KEY = "suer:user:update:phone:";
    /**
     * 用户更新电话过期时间
     */
    public static final Long USER_UPDATE_PHONE_TTL = 15L;
    /**
     * 用户更新邮件键
     */
    public static final String USER_UPDATE_EMAIL_KEY = "suer:user:update:email:";
    /**
     * 用户更新邮件过期时间
     */
    public static final Long USER_UPDATE_EMAIL_TTL = 15L;
    /**
     * 用户忘记密码键
     */
    public static final String USER_FORGET_PASSWORD_KEY = "xuyuan:user:forget:";
    /**
     * 用户忘记密码过期时间
     */
    public static final Long USER_FORGET_PASSWORD_TTL = 15L;
    /**
     * 博客推送键
     */
    public static final String BLOG_FEED_KEY = "xuyuan:feed:blog:";
    /**
     * 新博文消息键
     */
    public static final String MESSAGE_BLOG_NUM_KEY = "xuyuan:message:blog:num:";
    /**
     * 新点赞消息键
     */
    public static final String MESSAGE_LIKE_NUM_KEY = "xuyuan:message:like:num:";
    /**
     * 用户推荐缓存
     */
    public static final String USER_RECOMMEND_KEY = "xuyuan:recommend:";
    /**
     * 用户推荐缓存
     */
    public static final String USER_MATCH_KEY = "xuyuan:match:";

    /**
     * 最小缓存随机时间
     */
    public static final int MINIMUM_CACHE_RANDOM_TIME = 2;
    /**
     * 最大缓存随机时间
     */
    public static final int MAXIMUM_CACHE_RANDOM_TIME = 5;
    /**
     * 缓存时间偏移
     */
    public static final int CACHE_TIME_OFFSET = 10;
    /**
     * recommand定期缓存时间
     */
    public static final String RECOMMAN_LAST_KEY = "yupao:user:recommend";

}
