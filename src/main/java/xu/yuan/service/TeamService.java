package xu.yuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xu.yuan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.*;
import xu.yuan.model.vo.TeamUserVo;
import xu.yuan.model.vo.TeamVO;
import xu.yuan.model.vo.UserVO;

import java.util.List;

/**
* @author 肖广龙
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-06-07 17:13:26
*/
public interface TeamService extends IService<Team> {
    /**
     * 校验,并创建用户
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);



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
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param logUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User logUser);

    boolean deleteTeam(long teamId, User loginUser);

    /**
     * 获取队伍信息
     * @param currentPage
     * @param teamRequst
     * @param admin
     * @return
     */
    Page<TeamVO> listTeams(long currentPage, TeamRequst teamRequst, boolean admin);

    /**
     * 获取队伍加入者照片
     * @param teamVoPage
     * @return
     */
    Page<TeamVO> getJoinedUserAvatarUrl(Page<TeamVO> teamVoPage);

    TeamVO getTeam(Long id, long id1);

    List<UserVO> getTeamMember(Long id, long id1);


    void kickOut(Long teamId, Long userId, long id, boolean admin);


    void changeCoverImage(TeamUpdateAvart teamUpdateAvart, long id, boolean admin);


    Page<TeamVO> getCreateUserWithTeam(long currentPage, long id, TeamRequst teamRequst);


    Page<TeamVO> getCreateUserWithJoinTeam(long currentPage, long id, TeamRequst teamRequst);


    List<TeamVO> MessagelistAllMyJoin(long id);
}
