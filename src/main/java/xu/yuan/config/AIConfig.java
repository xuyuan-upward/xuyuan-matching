package xu.yuan.config;

import com.zhipu.oapi.ClientV4;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xu.yuan.properties.AIProperties;

import javax.annotation.Resource;

/**
 * AI 配置类
 *
 * @author xuyuan
 */
@Configuration
public class AIConfig {

    @Resource
    private AIProperties aiProperties;

    @Bean
    public ClientV4 clientV4() {
        if (Boolean.TRUE.equals(aiProperties.getEnable())) {
            return new ClientV4.Builder(aiProperties.getKey()).build();
        } else {
            return new ClientV4();
        }
    }
}
