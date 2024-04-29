package xu.yuan;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;

@SpringBootTest
class YuPaoApplicationTests {

    @Resource
    private RedissonClient redissonClient;
    @Test
    void contextLoads() {


    }

}
