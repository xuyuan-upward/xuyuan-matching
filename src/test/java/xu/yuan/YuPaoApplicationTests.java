package xu.yuan;

import cn.hutool.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import xu.yuan.mapper.TeamMapper;
import xu.yuan.model.domain.Team;
import xu.yuan.service.TeamService;
import javax.annotation.Resource;

@SpringBootTest
class YuPaoApplicationTests {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private TeamService teamService;
    @Test
    void contextLoads() {
        Team team = new Team();
        team.setId(1L);
        team.setDescription("dfdfd");
        teamService.updateById(team);
    }
    @Test
    void test(){
        JSONObject jsonObject = new JSONObject();
        JSONObject set = jsonObject.set("quyuh", "fdjfd");
        System.out.println(set.toString());
    }
}
