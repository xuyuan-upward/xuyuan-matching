package xu.yuan.Eception;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
import lombok.Data;
import xu.yuan.Common.ErrorCode;

/**
 * 自定义异常类
 */
@Data
public class BusinessEception extends RuntimeException{
    private int code;

    private String description;

    public BusinessEception(int code, String description,String message){
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessEception(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessEception(ErrorCode errorCode,String description){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
