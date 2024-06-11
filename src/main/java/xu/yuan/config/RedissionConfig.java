package xu.yuan.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissionConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;
//    @Value("${spring.redis.password}")
//    private String password;

    @Bean
    public RedissonClient redissonClient(){
        // 1. Create config object
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
//        config.useSingleServer().setAddress(address).setPassword(password);
        config.useSingleServer().setAddress(address);
        // 2. Create Redisson instance(创建实例)
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }


}
