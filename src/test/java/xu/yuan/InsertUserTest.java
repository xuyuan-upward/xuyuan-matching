package xu.yuan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;
import xu.yuan.mapper.UserMapper;
import xu.yuan.model.domain.User;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class InsertUserTest {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 批量插入
     */
@Test
    public void doInsertUsers(){
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    final int batchSize = 5000;
    int j = 0;
    ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
        List<User> userArrayList = Collections.synchronizedList(new ArrayList<>());
        while (true) {
            j++;
            User user = new User();
            user.setUsername("假许苑");
            user.setUseraccount("fakexuyuan");
            user.setAvatarurl("https://tse4-mm.cn.bing.net/th/id/OIP-C.DrGC1BblAyZ5C3SxbqFYCwAAAA?w=199&h=199&c=7&r=0&o=5&dpr=2&pid=1.7");
            user.setGender(0);
            user.setUserpassword("12345678");
            user.setPhone("18778959139");
            user.setEmail("2517115657@qq.com");
            user.setUserstatus(0);
            user.setRole(0);
            user.setPlanetcode("111111");
            userArrayList.add(user);
            if (j % batchSize ==0 ){
                break;
            }
        }
        //异步执行
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("threadname:" + Thread.currentThread().getName());
            userService.saveBatch(userArrayList, batchSize);
        });
        futureList.add(future);
    }
    CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
    stopWatch.stop();
    System.out.println(stopWatch.getTotalTimeMillis());
    }

   /* @Test
    public void RedisTest(){

        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set();
    }*/

}


