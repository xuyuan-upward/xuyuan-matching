package xu.yuan.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xu.yuan.model.domain.User;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    //重点用户
   private List<Long> mainUserList = Arrays.asList(1L);
    //每天执行
    @Scheduled(cron = "59 59 23 * * *")
    public void preCacheJob() {
        ValueOperations<String, Object> redis = redisTemplate.opsForValue();
        RLock lock = redissonClient.getLock("lock:pre:xuyuan");
        try {
            if ( lock.tryLock(0,30000L,TimeUnit.MILLISECONDS)) {
                for (Long userId : mainUserList) {
                    // key的命名
        //        String key = String.format("yupao:user:recommend:%s", logUser.getId());
                    String key = String.format("yupao:user:recommend:$s",userId);
                    //没有,查询数据库
                    LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
                    Page<User> page = new Page<>(1, 20);
                    Page<User> pageList = userService.page(page, lqw);
                    // 防止设置key时候错误还是返回数据
                    try {
                        //并写入redis中
                        redis.set(key, pageList, 1, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.info("redis set key: error {}", e);
                    }


                }
            }
        } catch (InterruptedException e) {
            log.info("获取锁,出现异常");
        }finally {
            // 判断是否是自己的锁
            if (lock.isHeldByCurrentThread())
            lock.unlock();
        }
    }
}
