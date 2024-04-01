package xu.yuan.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
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
     *  用户登录态键
     */


    @Override
    public long registerUser(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return -1;
        }
        if (userAccount.length() < 4) {
            return -1;
        }
        if (userPassword.length() < 8) {
            return -1;
        }
        String regEx = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(userAccount);

        if (m.find()) {
            return -1;
        }
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        //用户不能重复
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUseraccount, userAccount);
        int count = this.count(userLambdaQueryWrapper);
        if (count > 0) {
            return -1;
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUseraccount(userAccount);
        user.setUserpassword(encryptPassword);
        boolean save = this.save(user);
        if (!save) {
            return -1;
        }
        return user.getId();

    }

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
        //用户不能重复
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
        safeUser.setUserstatus(user.getUserstatus());
        return safeUser;
    }
}




