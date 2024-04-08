package xu.yuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xu.yuan.Common.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.User;
import xu.yuan.service.UserService;
import xu.yuan.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xu.yuan.Constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 肖广龙
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-03-26 18:25:13
 */
@Service
@Slf4j

public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 盐值，混淆
     */
    private static final String SALT = "xuyuan";
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long registerUser(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
          throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        if (userAccount.length() < 4) {
           throw new BusinessEception(ErrorCode.PARAMS_ERROR,"用户名输入不合法");
        }
        if (userPassword.length() < 8) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"密码输入不合法");
        }
        if (planetCode.length() > 5) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"编号输入不合法");
        }
        String regEx = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(userAccount);

        if (m.find()) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"用户名输入不合法");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"两次密码输入不一致");

        }
        //用户不能重复
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUseraccount, userAccount).or().eq(User::getPlanetcode,planetCode);
        int count = this.count(userLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"用户名或编号已经存在");
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUseraccount(userAccount);
        user.setUserpassword(encryptPassword);
        user.setPlanetcode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"用户名或编号已经存在");
        }
        return user.getId();

    }

    /**
     * 登录校验
     * @param userAccount
     * @param userPassword
     * @param httpServletRequest
     * @return
     */
    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        String regEx = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(userAccount);

        if (m.find()) {
            return null;
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //用户密码校验
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUseraccount, userAccount)
                .eq(User::getUserpassword, encryptPassword);
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        if (user == null) {
            log.info("password error or account error");
            return null;
        }
      //用户脱敏
        User saftyUser = getSaftyUser(user);

        //记录用户登录状态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, saftyUser);
        return saftyUser;

    }

    /**
     * 用户返回前端脱敏
     * @param user
     * @return
     */
    @Override
    public User getSaftyUser(User user){
        //用户脱敏
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUseraccount(user.getUseraccount());
        safeUser.setAvatarurl(user.getAvatarurl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setPlanetcode(user.getPlanetcode());
        safeUser.setUserstatus(user.getUserstatus());
        return safeUser;
    }
    /**
     * 用户注销
     */
    @Override
    public int userLogout(HttpServletRequest httpServletRequest) {
        //移除登录态
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




