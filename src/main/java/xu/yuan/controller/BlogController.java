package xu.yuan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.BlogAddRequest;
import xu.yuan.model.request.BlogUpdateRequest;
import xu.yuan.model.vo.BlogVO;
import xu.yuan.service.BlogService;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/blog")
public class BlogController {
    /**
     * 博客
     */
    @Resource
    private BlogService blogService;

    /**
     * 用户
     */
    @Resource
    private UserService userService;
    /**
     * 添加博客
     *
     * @param blogAddRequest 博客添加请求
     * @param request        请求
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加博文")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "blogAddRequest", value = "博文添加请求"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> addBlog(BlogAddRequest blogAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (StringUtils.isAnyBlank(blogAddRequest.getTitle(), blogAddRequest.getContent())) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        blogService.addBlog(blogAddRequest, loginUser);
        return ResultUtils.success("添加成功");
    }

    /**
     *
     * @param currentPage 当前页
     * @param searchText 关键字
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取博文列表")
    public Result<Page<BlogVO>> getBlogList(long currentPage, String searchText, HttpServletRequest request) {
        // 获取当前页面
        if (currentPage <= 0) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User logUser = userService.getLogUser(request);
        if (logUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        Page<BlogVO> blogVOPage = blogService.getList(currentPage, searchText, logUser.getId());
        return ResultUtils.success(blogVOPage);
    }
    /**
     * 通过id获取博客
     *
     * @param id      id
     * @param request 请求
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取博文")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "博文id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<BlogVO> getBlogById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (id == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(blogService.getBlogById(id, loginUser.getId()));
    }

    /**
     * 删除博客通过id
     *
     * @param id      id
     * @param request 请求
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除博文")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "博文id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> deleteBlogById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (id == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        boolean admin = userService.isAdmin(loginUser);
        blogService.IsAuthriotyDeleteBlog(id, loginUser.getId(), admin);
        return ResultUtils.success("删除成功");
    }


    /**
     * 删除博客通过id
     *
     * @param blogUpdateRequest      修改请求
     * @param request 请求
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改博文")
    public Result<String> deleteBlogById( BlogUpdateRequest blogUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (blogUpdateRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        // 当前作者id
        Long id = blogUpdateRequest.getId();
        // 判断当前登录用户是否是管理员
        boolean admin = userService.isAdmin(loginUser);
        blogService.IsAuthriotyUpdateBlog(id, loginUser, admin,blogUpdateRequest);
        return ResultUtils.success("修改成功");
    }
}
