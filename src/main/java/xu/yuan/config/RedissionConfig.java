package xu.yuan.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedissionConfig {

    @Bean
    public RedissonClient redissonClient(){
        // 1. Create config object
        Config config = new Config();
        String redissAdress = "redis://127.0.0.1:6379";
        config.useSingleServer().setAddress(redissAdress).setDatabase(3);

        // 2. Create Redisson instance(创建实例)
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }


}
