package xu.yuan.service;

import xu.yuan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.domain.User;
import xu.yuan.model.dto.TeamQuery;
import xu.yuan.model.request.TeamJoinRequest;
import xu.yuan.model.request.TeamQuitRequest;
import xu.yuan.model.request.TeamUpdateRequest;
import xu.yuan.model.vo.TeamUserVo;

import java.util.List;

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


    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 队伍加入是否成功
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param logUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User logUser);

    boolean deleteTeam(long teamId, User loginUser);

    /**
     * 获取我所有加入的队伍
     * @param id
     * @return
     */
    List<TeamUserVo> listAllMyJoin(long id);
}
