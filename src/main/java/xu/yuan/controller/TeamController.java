package xu.yuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.dto.TeamQuery;

import xu.yuan.model.request.TeamRequest;
import xu.yuan.service.TeamService;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static xu.yuan.Constant.UserConstant.USER_LOGIN_STATE;

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
    @PostMapping("/add")
    public Result<Long> addTeam(@RequestBody TeamRequest teamRequest, HttpServletRequest request){
        if (teamRequest == null){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User logUser = userService.getLogUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamRequest,team);
        long teamId = teamService.addTeam(team,logUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public Result<Boolean> deleteTeam( long id){
        if (id <= 0){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        boolean save = teamService.removeById(id);
        if (!save)
        {
            throw new BusinessEception(ErrorCode.SYSTEM, "删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public Result<Boolean> updateTeam(@RequestBody Team team){
        if (team == null){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        boolean save = teamService.updateById(team);
        if (!save)
        {
            throw new BusinessEception(ErrorCode.SYSTEM, "更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public Result<Team> getTeam( long id){
        if (id <= 0){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null)
        {
            throw new BusinessEception(ErrorCode.SYSTEM, "查询失败");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list/page")
    public Result<Page<Team>> getTeam(@RequestBody TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        Page<Team> newPage = teamService.page(teamPage, wrapper);
        return ResultUtils.success(newPage);
    }
}

