package xu.yuan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.User;
import xu.yuan.model.vo.UserVO;
import xu.yuan.service.FollowService;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/follow")
public class FollowController {
    /**
     * 关注服务
     */
    @Resource
    private FollowService followService;

    @Resource
    private UserService userService;

    /**
     * 关注用户
     *
     * @param id      id
     * @param request 请求
     */
    @PostMapping("/{id}")
    @ApiOperation(value = "关注用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "关注用户id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> followUser(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        followService.FollowUser(id, loginUser.getId());
        return ResultUtils.success("ok");
    }

    /**
     * 列出粉丝
     *
     * @param request     请求
     * @param currentPage 当前页码
     */
    @GetMapping("/fans")
    @ApiOperation(value = "获取粉丝")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<Page<UserVO>> listFans(HttpServletRequest request, long currentPage) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        Page<UserVO> userVoPage = followService.pageFans(loginUser.getId(), currentPage);
        return ResultUtils.success(userVoPage);
    }

    /**
     * 获取我关注的用户
     *
     * @param request     请求
     * @param currentPage 当前页码
     */
    @GetMapping("/my")
    @ApiOperation(value = "获取我关注的用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<Page<UserVO>> listMyFollow(HttpServletRequest request, long currentPage) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        Page<UserVO> userVoPage = followService.pageMyFollow(loginUser.getId(), currentPage);
        return ResultUtils.success(userVoPage);
    }
}
