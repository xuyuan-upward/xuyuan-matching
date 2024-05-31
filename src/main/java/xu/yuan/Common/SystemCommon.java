package xu.yuan.Common;

public class SystemCommon {
    /**
     * 最大验证码
     */
    public static final int MAXIMUM_VERIFICATION_CODE_NUM = 999999;
    /**
     * 最小验证码
     */
    public static final int MINIMUM_VERIFICATION_CODE_NUM = 100000;

    /**
     * 注册验证码键
     */
    public static final String REGISTER_CODE_KEY = "xuyuan:register:"; /**
     * 忘记密码的验证码键
     */
    public static final String USER_FORGET_PASSWORD_KEY = "xuyuan:foreget:";
    /**
     * 注册验证码过期时间
     */
    public static final Long REGISTER_CODE_TTL = 15L;
    /**
     * 注册验证码过期时间
     */
    public static final Long USER_FORGET_PASSWORD_CODE_TTL = 2L;
}
