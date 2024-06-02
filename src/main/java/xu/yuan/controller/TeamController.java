package xu.yuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import xu.yuan.Common.DeleteRequest;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.model.dto.TeamQuery;

import xu.yuan.model.request.TeamAddRequest;
import xu.yuan.model.request.TeamJoinRequest;
import xu.yuan.model.request.TeamQuitRequest;
import xu.yuan.model.request.TeamUpdateRequest;
import xu.yuan.model.vo.TeamUserVo;
import xu.yuan.service.TeamService;
import xu.yuan.service.UserService;
import xu.yuan.service.UserTeamService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/team")
@Slf4j
//允许这个IP跨域
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    /**
     * 加入队伍
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Result<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User logUser = userService.getLogUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, logUser);
        return ResultUtils.success(teamId);
    }
    @PostMapping("/update")
    public Result<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        boolean save = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!save) {
            throw new BusinessEception(ErrorCode.SYSTEM, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public Result<Team> getTeam(long id) {
        if (id <= 0) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessEception(ErrorCode.SYSTEM, "查询失败");
        }
        return ResultUtils.success(team);
    }

   /* @GetMapping("/list")
    public Result<List<Team>> getTeam(@RequestBody TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        Page<Team> newPage = teamService.page(teamPage, wrapper);
        return ResultUtils.success(newPage);
    }*/

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public Result<List<TeamUserVo>> myjoingetTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
          User logUser = userService.getLogUser(request);

        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", logUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(wrapper);
        // key: teamId    values: userId
        // 1, 2
        // 2, 2
        // 3, 3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        // 获取当前用户所有加入队伍的teamId
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        // 获取当前用户所有加入队伍的team的字段Id
        teamQuery.setTeamId(idList);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVos);
    }
    /**
     * 列出所有我加入团队
     *
     * @param request 请求
     * @return {@link }<{@link List}<{@link }>>
     */
    @GetMapping("/list/my/join/all")
    @ApiOperation(value = "获取我加入的队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<TeamUserVo>> listAllMyJoinTeams(HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        List<TeamUserVo> teamVOList = teamService.listAllMyJoin(loginUser.getId());
        return ResultUtils.success(teamVOList);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public Result<List<TeamUserVo>> myCreateTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
          User logUser = userService.getLogUser(request);
        long userId = logUser.getId();
        teamQuery.setUserId(userId);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVos);
    }

    /**
     * 获取公开或者加密的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }

        boolean isAdmin = userService.isAdmin(request);
        // 1、查询各个队伍列表 根据teamQuery传递的各种参数进行模糊查询
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, true);
        if (teamList == null || teamList.size() == 0) {
            return ResultUtils.success(teamList);
        }
        // 每个队伍的Id
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLogUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            // 判断每个队伍id是否已经加入
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        // 查询出来有多少个是teamId的队伍
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public Result<Page<Team>> getTeamPages(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        Page<Team> newPage = teamService.page(teamPage, wrapper);
        return ResultUtils.success(newPage);
    }

    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public Result<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
          User logUser = userService.getLogUser(request);
        boolean save = teamService.joinTeam(teamJoinRequest,logUser);
        return ResultUtils.success(save);
    }

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public Result<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User logUser = userService.getLogUser(request);
        boolean save = teamService.quitTeam(teamQuitRequest,logUser);
        return ResultUtils.success(save);
    }

    /**
     * 队长解散队伍
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null ||deleteRequest.getId() <= 0) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        long teamId = deleteRequest.getId();
        User logUser = userService.getLogUser(request);
        boolean save = teamService.deleteTeam(teamId,logUser);
        if (!save) {
            throw new BusinessEception(ErrorCode.SYSTEM, "删除失败");
        }
        return ResultUtils.success(true);
    }
}

