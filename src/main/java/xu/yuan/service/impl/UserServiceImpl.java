package xu.yuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.domain.User;
import xu.yuan.service.UserService;
import xu.yuan.mapper.UserMapper;
import org.springframework.stereotype.Service;
import xu.yuan.utils.AlgorithmUtils;
import xu.yuan.utils.AliOSSUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xu.yuan.Common.SystemCommon.REGISTER_CODE_KEY;
import static xu.yuan.Common.SystemCommon.USER_FORGET_PASSWORD_KEY;
import static xu.yuan.Constant.UserConstant.ADMIN_ROLE;
import static xu.yuan.Constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 许苑
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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private AliOSSUtils aliOSSUtils;
    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long registerUser(String userAccount, String userPassword, String checkPassword, String userName,String phone,String code) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户名输入不合法");
        }
        if (userPassword.length() < 8) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "密码输入不合法");
        }

        String regEx = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(userAccount);

        if (m.find()) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户名输入不合法");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }
        //校验验证码
        String rightCode = (String) redisTemplate.opsForValue().get(REGISTER_CODE_KEY + phone);
        if (!rightCode.equals(code)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        //用户不能重复
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUseraccount, userAccount);
        int count = this.count(userLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户名已经存在");
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUseraccount(userAccount);
        user.setUserpassword(encryptPassword);
        user.setPhone(phone);
        user.setUsername(userName);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户名或编号已经存在");
        }
        return user.getId();

    }

    /**
     * 登录校验
     *
     * @param userAccount
     * @param userPassword
     * @param
     * @return
     */
  /*  @Override
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
        log.info("saftyUser:{}",saftyUser);
        //记录用户登录状态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, saftyUser);
        return saftyUser;
    }*/
    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSaftyUser(user);
        // 4. 记录用户的登录态 此是session存放到了redis中
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户返回前端脱敏
     *
     * @param user
     * @return
     */
    @Override
    public User getSaftyUser(User user) {
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
        safeUser.setTags(user.getTags());
        safeUser.setRole(user.getRole());
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

    /**
     * 内存使用
     *
     * @param tagList
     * @return
     */
    @Override
    public List<User> searchUserByTag(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        // 先查询所有用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(wrapper);
        Gson gson = new Gson();
        //然后在内存中判断包含要求的标签
        return userList.stream().filter(user -> {
            String tags = user.getTags();
            Set<String> tempListags = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            //去封装可能为空的对象，如果为空返回new HashSet<>()给你
            tempListags = Optional.ofNullable(tempListags).orElse(new HashSet<>());
            for (String tag : tagList) {
                if (!tempListags.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSaftyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User logUser) {
        // 仅管理员和自己修改
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        // todo 如果传入一个id（就是没有其他数据修改时候会报错）
        if (!isAdmin(logUser) && userId != logUser.getId()) {
            throw new BusinessEception(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }

        return userMapper.updateById(user);
    }

    @Override
    public User getLogUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    /**
     * 判断是否为管理员
     *
     * @param logUser
     * @return
     */
    @Override
    public boolean isAdmin(User logUser) {
        // 如果是管理员
        return (logUser != null && logUser.getRole() == ADMIN_ROLE);
    }

    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest) {
        // 如果是管理员可查

        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);

        return (user != null && user.getRole() == ADMIN_ROLE);
    }

    /**
     * 匹配对应的用户
     *
     * @param num
     * @param logUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User logUser) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "tags", "avatarurl", "planetcode", "username");
        wrapper.isNotNull("tags");
        List<User> userList = this.list(wrapper);
        String tags = logUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        List<User> users = null;
        if (CollectionUtils.isEmpty(tagList)) {
            int i = 0;
            for (User user : userList) {
                if (user.getId() == logUser.getId()) {
                    userList.remove(i);
                    break;
                }
                i++;
            }
            users = userList;
            return users;
        }
        // 用户列表的下标 => 相似度
        // pair保存的是一对key value，而map可以保存多对key value。
        // 即:pair => (1,3)    map => (1,3),(2,3),(3,3)
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者标签为当前用户
            if (StringUtils.isBlank(userTags) || user.getId() == logUser.getId()) {
                continue;
            }
            List<String> usertagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数 分数越小表明契合度越高 =>3, 4  表明3的代表的tags匹配契合度更高
            long distance = AlgorithmUtils.minDistance(tagList, usertagList);
            // 相当于计算所有用户的tags相似度
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserrPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue())) // .sorted表示根据值进行排序
                .limit(num)
                .collect(Collectors.toList());
        List<User> userVOlist = topUserrPairList.stream()
                .map(Pair::getKey)       // map是用来进行类型转换的
                .collect(Collectors.toList());
        // 用户脱敏
        users = userVOlist.stream()
                .map(this::getSaftyUser)
                .collect(Collectors.toList());

        // 鱼皮的比较复杂:
       /* // 原本编辑顺序的 userId列表
        List<Long> userVOlist = topUserrPairList.stream()
                .map(pair -> pair.getKey().getId())       // map是用来进行类型转换的
                .collect(Collectors.toList());
        // 用户脱敏
        wrapper = new QueryWrapper<>();
        // 根据userid查询出来所有的用户
        wrapper.in("id", userVOlist);
//        user -> getSaftyUser(user)
        Map<Long, List<User>> longListMap = this.list(wrapper)
                .stream()
                .map(this::getSaftyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> FinalUserList = new ArrayList<>();
        // 作用是将排序好的 userVolist 取出来重新放入一个新的集合里面
        for (Long userId : userVOlist) {
            FinalUserList.add(longListMap.get(userId).get(0));
        }*/
        return users;

    }

    @Override
    public void updateTags(List<String> tags, long id) {
        User user = new User();
        Gson gson = new Gson();
        String userTags = gson.toJson(tags);
        System.out.println(userTags);
        user.setTags(userTags);
        user.setId(id);
        boolean flag = this.updateById(user);
        if (!flag) {
            throw new BusinessEception(ErrorCode.SYSTEM);
        }
    }

    /**
     * 发送验证码或者修改密码
     * @param phone
     * @param
     * @param password
     * @param confirmPassword
     */
    @Override
    public void  updatePassword(String phone, String password, String confirmPassword,HttpServletRequest request) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
//        String key = USER_FORGET_PASSWORD_KEY + phone;
//
//        String correctCode = (String) redisTemplate.opsForValue().get(key);
        // 这一步可以直接省略
//
//        if (!correctCode.equals(code)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
//        }
        // 1.根据当前手机号查询当前用户
//        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        userLambdaQueryWrapper.eq(User::getPhone, phone);
//        User user = this.getOne(userLambdaQueryWrapper);
        // 2.根据当前登录用户查找当前信息
        User logUser = this.getLogUser(request);
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        logUser.setUserpassword(encryptPassword);
        this.updateById(logUser);
        // 用户注销
        int i = userLogout(request);
        if (i < 0) {
            throw new BusinessEception(ErrorCode.SYSTEM,"系统异常");
        }

    }

    /**
     * sql
     *
     * @param tagList
     * @return
     */
    @Deprecated
    private List<User> searchUserByTa1(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        for (String tagName : tagList) {
            wrapper = wrapper.like("tags", tagName);
        }
        List<User> users = userMapper.selectList(wrapper);
        return users.stream().map(this::getSaftyUser).collect(Collectors.toList());
    }
}




