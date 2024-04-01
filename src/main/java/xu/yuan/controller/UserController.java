package xu.yuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xu.yuan.model.User;
import xu.yuan.model.UserLoginRequest;
import xu.yuan.model.UserRegisterRequest;
import xu.yuan.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static xu.yuan.Constant.UserConstant.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public Long UserRgister(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        return userService.registerUser(userAccount, userPassword, checkPassword);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public User Userlogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        return userService.doLogin(userAccount, userPassword, httpServletRequest);
    }

    /**
     * 用户根据userrname查询
     *
     * @param username
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/search")
    public List<User> searchUser(String username, HttpServletRequest httpServletRequest) {
        //仅管理员可查
        if (!isAdmin(httpServletRequest)) {
            return new ArrayList<>();
        }
        if (StringUtils.isAnyBlank(username)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.like(User::getUsername, username);
        List<User> list = userService.list(lqw);
        return list.stream().map(user -> userService.getSaftyUser(user)).collect(Collectors.toList());
    }

    @PostMapping("/delete")
    public boolean DeleteUser(@RequestBody long id, HttpServletRequest httpServletRequest) {
        //仅管理员可查
        if (!isAdmin(httpServletRequest)) {
            return false;
        }
        if (id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    /**
     * 是否为管理员
     * @param httpServletRequest
     * @return
     */
    public boolean isAdmin(HttpServletRequest httpServletRequest){

        //仅管理员可查
        User user = (xu.yuan.model.User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);

        return (user != null && user.getRole() == ADMIN_ROLE);
    }
}
