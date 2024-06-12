package xu.yuan.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import xu.yuan.Common.DeleteRequest;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.model.request.*;

import xu.yuan.model.vo.TeamUserVo;
import xu.yuan.model.vo.TeamVO;
import xu.yuan.model.vo.UserVO;
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
     * 添加队伍
     *
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
        if (logUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, logUser);
        return ResultUtils.success(teamId);
    }
    /**
     * 我创建团队名单
     *
     * @param currentPage 当前页面
     * @param teamRequst   团队查询
     * @param request     请求
     */
    @GetMapping("/list/my/create")
    @ApiOperation(value = "获取我创建的队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<Page<TeamVO>> listMyCreateTeams(long currentPage,
                                                        TeamRequst teamRequst,
                                                        HttpServletRequest request) {
        if (teamRequst == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        teamRequst.setUserId(loginUser.getId());
        // 返回我创建人的信息，已经其他队伍信息
        Page<TeamVO> teamVOPage = teamService.getCreateUserWithTeam(currentPage, loginUser.getId(),teamRequst);
        // 获取
        Page<TeamVO> teamVoPageWithAvatar = teamService.getJoinedUserAvatarUrl(teamVOPage);
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVoPageWithAvatar);
        return getTeamIsJoinList(loginUser, finalPage);
    }


    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param request
     * @return
     */
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

    /**
     * 获取队伍信息
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<TeamVO> getTeam(long id,HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        Long teamId = Long.valueOf(id);
        return ResultUtils.success(teamService.getTeam(teamId, loginUser.getId()));
    }


    /**
     * 列出所有我加入的团队
     *
     * @param request 请求
     * @return {@link }<{@link List}<{@link }>>
     */
    @GetMapping("/list/my/join")
    @ApiOperation(value = "获取我加入的队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<Page<TeamVO>> listAllMyJoinTeams(long currentPage,
                                                       TeamRequst teamRequst, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        // 返回我创建人的信息，已经其他队伍信息
        Page<TeamVO> teamVOPage = teamService.getCreateUserWithJoinTeam(currentPage, loginUser.getId(),teamRequst);
        // 获取
        Page<TeamVO> teamVoPageWithAvatar = teamService.getJoinedUserAvatarUrl(teamVOPage);
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVoPageWithAvatar);
        return getTeamIsJoinList(loginUser, finalPage);
    }

    /**
     * 列出所有我加入团队
     *
     * @param request 请求
     */
    @GetMapping("/list/my/join/all")
    @ApiOperation(value = "获取我加入的队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamQuery", value = "获取队伍请求参数"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<TeamVO>> listAllMyJoinTeams(HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        List<TeamVO> teamVOList = teamService.MessagelistAllMyJoin(loginUser.getId());
        return ResultUtils.success(teamVOList);
    }
    /**
     * 获取公开或者加密的队伍
     *
     * @param teamRequst
     * @param request
     * @return
     */
    @GetMapping("/list")
    public Result<Page<TeamVO>> listTeams(long currentPage, TeamRequst teamRequst, HttpServletRequest request) {
        // 判断当前有没有进行登录
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN, "没有登录");
        }
        // 获取队伍条件后的分页队伍信息 （主要包括创建者的信息，和一些队伍的基本信息
        Page<TeamVO> teamVoPage = teamService.listTeams(currentPage, teamRequst, userService.isAdmin(loginUser));
        // 获取加入队伍的队友照片
        Page<TeamVO> teamVoPageWithAvatar = teamService.getJoinedUserAvatarUrl(teamVoPage);
        // 获取队伍的人数
        Page<TeamVO> finalPage = getTeamHasJoinNum(teamVoPageWithAvatar);
        return getTeamIsJoinList(loginUser, finalPage);
    }

    /**
     * 通过id获取团队
     *
     * @param id      id
     * @param request 请求
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询队伍")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "队伍id")})
    public Result<TeamVO> getTeamById(@PathVariable Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(teamService.getTeam(id, loginUser.getId()));
    }

    /**
     * 获取登录用户已经加入了哪些队伍
     *
     * @param loginUser
     * @param finalPage
     * @return
     */
    private Result<Page<TeamVO>> getTeamIsJoinList(User loginUser, Page<TeamVO> finalPage) {
        // 获取当前登录用户id
        long loginUserId = loginUser.getId();
        // 根据条件搜索出来的队伍id
        List<Long> teamIdlist = finalPage.getRecords().stream().map(TeamVO::getId).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(teamIdlist)) {
            return ResultUtils.success(finalPage.setRecords(new ArrayList<TeamVO>()));
        }
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeam::getUserId, loginUserId).in(UserTeam::getTeamId, teamIdlist);
        // 包含了和当前登录用户有关的队伍，即为用户已经加入的队伍关系
        List<UserTeam> UserTeamlist = userTeamService.list(wrapper);
        // 获取用户已经加入的队伍id
        List<Long> UserHasTeamIds = UserTeamlist.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        // 根据条件搜所出来的所有队伍
        List<TeamVO> teamVOList = finalPage.getRecords();
        // 从中赛选
        teamVOList.stream().forEach(teamVO -> {teamVO.setHasJoin(UserHasTeamIds.contains(teamVO.getId()));
          /*  teamVO.setCreateTime(DateUtil.parse(DateUtil.format(teamVO.getCreateTime(), "yyyy-MM-dd HH:mm:ss")));
            teamVO.setExpireTime(DateUtil.parse(DateUtil.format(teamVO.getExpireTime(), "yyyy-MM-dd HH:mm:ss")));
            teamVO.setupda(DateUtil.parse(DateUtil.format(teamVO.getUpdateTime(), "yyyy-MM-dd HH:mm:ss")));*/
        });
        finalPage.setRecords(teamVOList);
        return ResultUtils.success(finalPage);
    }

    /**
     * 通过id获取团队成员
     *
     * @param id      id
     * @param request 请求
     */
    @GetMapping("/member/{id}")
    @ApiOperation(value = "获取队伍成员")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "队伍id"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<UserVO>> getTeamMemberById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (id == null || id < 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        List<UserVO> teamMember = teamService.getTeamMember(id, loginUser.getId());
        return ResultUtils.success(teamMember);
    }


    /**
     * 获取每个队伍的人数
     *
     * @param teamVoPageWithAvatar
     * @return
     */
    @Transactional
    public Page<TeamVO> getTeamHasJoinNum(Page<TeamVO> teamVoPageWithAvatar) {
        List<TeamVO> teamList = teamVoPageWithAvatar.getRecords();
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        teamList.forEach((team) -> {
            wrapper.eq(UserTeam::getTeamId, team.getId());
            long hasJoinNum = userTeamService.count(wrapper);
            team.setHasJoinNum(hasJoinNum);
            wrapper.clear();
        });
        teamVoPageWithAvatar.setRecords(teamList);
        return teamVoPageWithAvatar;
    }

    @GetMapping("/list/page")
    public Result<Page<Team>> getTeamPages(TeamRequst teamRequst) {
        if (teamRequst == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamRequst, team);
        Page<Team> teamPage = new Page<>(teamRequst.getPageNum(), teamRequst.getPageSize());
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        Page<Team> newPage = teamService.page(teamPage, wrapper);
        return ResultUtils.success(newPage);
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public Result<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User logUser = userService.getLogUser(request);
        boolean save = teamService.joinTeam(teamJoinRequest, logUser);
        return ResultUtils.success(save);
    }
    /**
     * 踢出队员
     *
     * @param teamKickOutRequest 踢出队员请求
     * @param request            请求
     */
    @PostMapping("/kick")
    @ApiOperation(value = "踢出队员")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamKickOutRequest", value = "踢出队员请求"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> kickOut(@RequestBody TeamKickOutRequest teamKickOutRequest,
                                        HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
       // 获取对应的参数
        Long teamId = teamKickOutRequest.getTeamId();
        Long KickUserId = teamKickOutRequest.getUserId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        if (KickUserId == null || KickUserId <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        boolean admin = userService.isAdmin(loginUser);
        teamService.kickOut(teamId, KickUserId, loginUser.getId(), admin);
        return ResultUtils.success("ok");
    }
    /**
     * 用户退出队伍
     *
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
        boolean save = teamService.quitTeam(teamQuitRequest, logUser);
        return ResultUtils.success(save);
    }

    /**
     * 队长解散队伍
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        long teamId = deleteRequest.getId();
        User logUser = userService.getLogUser(request);
        boolean save = teamService.deleteTeam(teamId, logUser);
        if (!save) {
            throw new BusinessEception(ErrorCode.SYSTEM, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新封面图片
     *
     * @param teamUpdateAvart 团队包括变更请求
     * @param request                请求
     */
    @PutMapping("/cover")
    @ApiOperation(value = "更新封面图片")
    @ApiImplicitParams({@ApiImplicitParam(name = "teamCoverUpdateRequest", value = "队伍封面更新请求"),
            @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> changeCoverImage(TeamUpdateAvart teamUpdateAvart,
                                           HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        boolean admin = userService.isAdmin(loginUser);
        teamService.changeCoverImage(teamUpdateAvart, loginUser.getId(), admin);
        return ResultUtils.success("ok");
    }

    //    @GetMapping("/list")
//    public Result<List<Team>> getTeam(@RequestBody TeamRequst teamRequst){
//        if (teamRequst == null){
//            throw new BusinessEception(ErrorCode.NULL_ERROR);
//        }
//        Team team = new Team();
//        BeanUtils.copyProperties(teamRequst,team);
//        Page<Team> teamPage = new Page<>(teamRequst.getPageNum(), teamRequst.getPageSize());
//        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
//        Page<Team> newPage = teamService.page(teamPage, wrapper);
//        return ResultUtils.success(newPage);
//    }
//
//    /**
//     * 获取我加入的队伍
//     *
//     * @param teamRequst
//     * @param request
//     * @return
//     */
//    @GetMapping("/list/my/join")
//    public Result<List<TeamUserVo>> myjoingetTeam(TeamRequst teamRequst, HttpServletRequest request) {
//        if (teamRequst == null) {
//            throw new BusinessEception(ErrorCode.NULL_ERROR);
//        }
//        User logUser = userService.getLogUser(request);
//
//        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
//        wrapper.eq("userId", logUser.getId());
//        List<UserTeam> userTeamList = userTeamService.list(wrapper);
//        // key: teamId    values: userId
//        // 1, 2
//        // 2, 2
//        // 3, 3
//        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
//        // 获取当前用户所有加入队伍的teamId
//        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
//        // 获取当前用户所有加入队伍的team的字段Id
//        teamRequst.setIdList(idList);
//        List<TeamUserVo> teamUserVos = teamService.listTeams(teamRequst, true);
//        return ResultUtils.success(teamUserVos);
//    }
//
//    /**
//     * 获取我创建的队伍
//     *
//     * @param teamRequst
//     * @param request
//     * @return
//     */
//    @GetMapping("/list/my/create")
//    public Result<List<TeamUserVo>> myCreateTeam(TeamRequst teamRequst, HttpServletRequest request) {
//        if (teamRequst == null) {
//            throw new BusinessEception(ErrorCode.NULL_ERROR);
//        }
//        User logUser = userService.getLogUser(request);
//        long userId = logUser.getId();
//        teamRequst.setUserId(userId);
//        List<TeamUserVo> teamUserVos = teamService.listTeams(teamRequst, true);
//        return ResultUtils.success(teamUserVos);
//    }

}

