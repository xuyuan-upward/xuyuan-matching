package xu.yuan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component //规定springIOC容器管理
@ConfigurationProperties(prefix = "xuyuan.oos")  //读取yml配置文件，并注入到其中去
public class OOSproperty {

    private String accessKey;

    private String endPoint;
    private String secretKey;
    private String bucketName;

    private String url;
}