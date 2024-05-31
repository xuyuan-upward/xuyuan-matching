package xu.yuan.utils;

import java.util.Random;

import static xu.yuan.Common.SystemCommon.MAXIMUM_VERIFICATION_CODE_NUM;
import static xu.yuan.Common.SystemCommon.MINIMUM_VERIFICATION_CODE_NUM;


/**
 * 验证码生成工具
 *
 *
 */
public final class ValidateCodeUtils {
    private ValidateCodeUtils() {
    }

    /**
     * 生成验证代码
     *
     * @return {@link Integer}
     */
    public static Integer generateValidateCode() {
     //   0-999999  111
        int code = new Random().nextInt(MAXIMUM_VERIFICATION_CODE_NUM); //生成随机数，最大为999999
        if (code < MINIMUM_VERIFICATION_CODE_NUM) {
            code = code + MINIMUM_VERIFICATION_CODE_NUM; //保证随机数为6位数字
        }
        return code;
    }
}
