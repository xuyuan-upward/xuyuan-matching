package xu.yuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import xu.yuan.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import xu.yuan.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 许苑
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
    long registerUser(String userAccount ,String userPassword
            ,String checkPassword
            ,String userName,String phone,String code);
    /**
     *  用户登录校验
     * @param userAccount
     * @param userPassword
     * @param httpServletRequest
     * @return
     */
    User doLogin(String userAccount , String userPassword, HttpServletRequest httpServletRequest);

    /**
     * 用户返回前端脱敏
     * @param user
     * @return
     */
    User getSaftyUser(User user);

    /**
     * 用户注销
     */
   int userLogout(HttpServletRequest httpServletRequest);

    /**
     * 根据标签搜索用户
     * @return
     */
    List<User> searchUserByTag(List<String> tagLiat);

    int updateUser(User user, User logUser);

    /**
     * 获取当前登录用户信息 但是获取不到对应用户的信息
     */
    public User getLogUser(HttpServletRequest request);

    boolean isAdmin(User logUser);

    public boolean isAdmin(HttpServletRequest httpServletRequest);

    /**
     * 返回互相匹配的用户
     * @param currentPage 表示第几页
     * @param logUser
     * @param username
     * @return
     */
    Page<User> matchUsers(long currentPage, User logUser, String username);

    void updateTags(List<String> tags, long id);

    void updatePassword(String phone, String password, String confirmPassword,HttpServletRequest request);

    /**
     * 获取对应私聊用户的信息
     * @param toId
     * @param id
     * @return
     */
    UserVO getUserById(Long toId, long id);
}

