package xu.yuan.service;

import xu.yuan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.domain.User;

/**
* @author 许苑
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-04-29 11:07:17
*/
public interface TeamService extends IService<Team> {

    /**
     * 校验,并创建用户
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
