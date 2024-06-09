package xu.yuan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "xuyuan.ai")
@Component
@Data
public class AIProperties {

    /**
     * 是否启用
     */
    private Boolean enable = false;

    /**
     * 密钥
     */
    private String key = null;
}
