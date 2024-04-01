package xu.yuan.service;

import org.springframework.stereotype.Service;
import xu.yuan.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 肖广龙
* @description 针对表【user】的数据库操作Service
* @createDate 2024-03-26 18:25:13
*/
@Service
public interface UserService extends IService<User> {


    /**
     *  用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long registerUser(String userAccount ,String userPassword ,String checkPassword);

    /**
     *  用户登录校验
     * @param userAccount
     * @param userPassword
     * @param httpServletRequest
     * @return
     */
    User doLogin(String userAccount , String userPassword, HttpServletRequest httpServletRequest);

    User getSaftyUser(User user);
}
