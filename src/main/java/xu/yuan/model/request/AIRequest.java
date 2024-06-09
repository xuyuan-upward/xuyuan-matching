package xu.yuan.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class AIRequest implements Serializable {

    private static final long serialVersionUID = -7432639416035181182L;
    /**
     * 用户消息
     */
    private String message;
}
