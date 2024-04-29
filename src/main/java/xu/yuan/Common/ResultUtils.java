package xu.yuan.Common;

import xu.yuan.enums.ErrorCode;

public class ResultUtils {
    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, data, "ok","");
    }
    /**
     * 失败返回信息
     *
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode);
    }

    public static <T> Result<T> error(ErrorCode errorCodeString ,String decription) {
     return new Result<>(errorCodeString,decription);
    }

    public static <T> Result<T> error(int code, String message, String description) {
       return new Result<>(code,null,message,description);
    }
}
