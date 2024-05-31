package xu.yuan.Eception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;

/**
 * 全局异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GloableEception {
    @ExceptionHandler(BusinessEception.class)
    public Result businessException(BusinessEception e) {
        log.error("异常处理："+e.getCode(),e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    /**
     * 系统异常
     * @param
     * @return
     */
   /* @ExceptionHandler(RuntimeException.class)
    public Result runtimeException(RuntimeException e) {
        return ResultUtils.error(ErrorCode.SYSTEM,"系统运行时异常");
    }
*/
}
