package xu.yuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.model.domain.Follow;
import xu.yuan.model.domain.User;
import xu.yuan.model.vo.UserVO;
import xu.yuan.service.FollowService;
import xu.yuan.service.UserService;
import xu.yuan.mapper.UserMapper;
import org.springframework.stereotype.Service;
import xu.yuan.utils.AlgorithmUtils;
import xu.yuan.utils.AliOSSUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xu.yuan.Common.SystemCommon.PAGE_SIZE;
import static xu.yuan.Common.SystemCommon.REGISTER_CODE_KEY;
import static xu.yuan.Constant.RedisConstants.USER_MATCH_KEY;
import static xu.yuan.Constant.UserConstant.ADMIN_ROLE;
import static xu.yuan.Constant.UserConstant.LOGIN_USER_KEY;

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
    /**
     * 用户服务
     */
    @Autowired
    private UserMapper userMapper;
    /**
     * redis操作
     */
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private AliOSSUtils aliOSSUtils;
    /**
     * 关注服务
     */
    @Resource
    private FollowService followService;

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long registerUser(String userAccount, String userPassword, String checkPassword, String userName, String phone, String code) {
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
        userLambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        int count = this.count(userLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户名已经存在");
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
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
        httpServletRequest.getSession().setAttribute(LOGIN_USER_KEY, saftyUser);
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
        // request.getSession()方法为每个用户请求创建或获取一个唯一的Session实例，
        // 所以即使所有用户都设置了同样的attribute名称（如LOGIN_USER_KEY），
        // 这些属性也是隔离的，存储在各自的Session中，通过Session ID区分，确保数据的独立性和准确性。

        // 最重要的一点就是 没有使用redis存储每个用户的用户信息，为什么呢？因为每个session是隔离的，虽然键一样
        // 但是每个sessionID是隔离的
        request.getSession().setAttribute(LOGIN_USER_KEY, safetyUser);
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
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setTags(user.getTags());
        safeUser.setRole(user.getRole());
        safeUser.setPersonality(user.getPersonality());
        return safeUser;
    }

    /**
     * 用户注销
     */
    @Override
    public int userLogout(HttpServletRequest httpServletRequest) {
        //移除登录态
        httpServletRequest.getSession().removeAttribute(LOGIN_USER_KEY);
        return 1;
    }

    /**
     * 根据标签进行选择
     *
     * @param tagList
     * @param currentPage
     * @return
     */
    @Override
    public Page<UserVO> searchUserByTag(List<String> tagList, long currentPage) {
        // 先查询所有用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 根据每条进行模糊匹配
        for (String tag : tagList) {
           wrapper = wrapper.or().like(StringUtils.isNotBlank(tag),User::getTags, tag);
        }
        Page<User> page = new Page<>(currentPage,PAGE_SIZE);
        Page<User> userpage = this.page(page, wrapper);
        Page<UserVO> userVOPage = new Page<UserVO>();
        /*使用 BeanUtils.copyProperties 方法时，它只会拷贝源对象和目标对象中具有相同名称和相同类型的属性。
        如果源对象和目标对象中的属性不完全匹配或属性是嵌套对象（如列表中的对象），则这些嵌套对象不会被深拷贝。
        在你的代码中，userpage 是 Page<User> 类型，userVOPage 是 Page<UserVO> 类型。BeanUtils.copyProperties(userpage, userVOPage);
         只会拷贝 Page 对象的属性（如总记录数、当前页码、每页记录数等），而不会自动拷贝 records 属性中 User 对象列表到 UserVO 对象列表。*/
        BeanUtils.copyProperties(userpage,userVOPage,"records");
        List<UserVO> userVOList = userpage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
       /* Gson gson = new Gson();
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
        }).map(this::getSaftyUser).collect(Collectors.toList());*/
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
        User user = (User) request.getSession().getAttribute(LOGIN_USER_KEY);
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

        User user = (User) httpServletRequest.getSession().getAttribute(LOGIN_USER_KEY);

        return (user != null && user.getRole() == ADMIN_ROLE);
    }

    /**
     * 匹配对应的心动用户
     *
     * @param currentPage 表示第几页
     * @param logUser
     * @param username
     * @return
     */
    @Override
    public Page<User> matchUsers(long currentPage, User logUser, String username) {
        Gson gson = new Gson();
        String MatchKey = USER_MATCH_KEY + logUser.getId() + currentPage;
        Page<User> userPage;
         String ToUserPageJson =   (String) redisTemplate.opsForValue().get(MatchKey);
         userPage = gson.fromJson(ToUserPageJson, new TypeToken<Page<User>>() {
        }.getType());
        // 根据用户名匹配
        if (StringUtils.isNotBlank(username)) {
            userPage = getUserNameLike(currentPage, username, logUser);
        }
        //根据匹配算法进行匹配
        else {
            // 判断是否有缓存
            if (redisTemplate.hasKey(MatchKey)) {
            }
            // 不存在缓存时候，去数据库找
            else {
                userPage = this.getMatchUsers(logUser, currentPage);
                if (userPage.getRecords() != null) {
                    // 转化成json数据保存到redis中去
                    String toJsonPage = gson.toJson(userPage);
                    redisTemplate.opsForValue().set(MatchKey,toJsonPage,2, TimeUnit.MINUTES);
                }
            }
        }
        return userPage;
    }

    /**
     * 获取分页心动用户
     *
     * @param logUser
     * @param currentPage
     * @return
     */
    private Page<User> getMatchUsers(User logUser, long currentPage) {

        String tags = logUser.getTags();
        if (StringUtils.isBlank(tags)) {
            // 获取全部用户 根据分页
            return this.getAllUser(currentPage);
        }
        // 获取心动值
        List<Pair<User, Long>> matchUsers = getMatchUsers(tags, logUser);
        // 获取最终结果
        List<User> LastUserList = matchUsers.stream().map((pari) -> pari.getKey()).collect(Collectors.toList());
        // 总记录数
        long totalSize = LastUserList.size();
        // 计算总页数，注意处理除不尽的情况，应向上取整
        int totalPages = (int) Math.ceil((double) totalSize / PAGE_SIZE);
        // 确保currentPage不超过总页数 并获取当前页数
        if (currentPage > totalPages) {
            return new Page<User>();
        }

        // 计算当前页的起始索引
        int startIndex = (int) ((currentPage - 1) * PAGE_SIZE);
// 获取当前页数据，注意处理索引越界问题
        List<User> currentPageUsers;
        if (startIndex >= totalSize) {
            // 如果开始索引已经超过总记录数，说明没有更多数据了
            currentPageUsers = Collections.emptyList();
        } else {
            int endIndex = (int) Math.min(startIndex + PAGE_SIZE, totalSize);
            currentPageUsers = LastUserList.subList(startIndex, endIndex);
        }
        // 创建Page对象并设置属性
        Page<User> userPage = new Page<>(currentPage, PAGE_SIZE);
        userPage.setRecords(currentPageUsers);
        userPage.setTotal(totalSize);
        userPage.setPages(totalPages);
        return userPage;

    }

    /**
     * 获取心动值键值对用户
     *
     * @param tags
     * @param loginUser
     * @return
     */
    private List<Pair<User, Long>> getMatchUsers(String tags, User loginUser) {
        long loginUserId = loginUser.getId();
        Gson gson = new Gson();
        //获取登录用户的标签，并转换为list
        List<String> LoginTags = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());
        // 用来存放各个匹配用户的心动分数 User 心动分数
        List<Pair<User, Long>> userMatchList = new ArrayList<>();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getAvatarUrl,User::getPersonality,User::getAvatarUrl,User::getTags,User::getUserAccount,User::getUsername);
        // 列出需要字段的所有用户
        List<User> userList = this.list(wrapper);
        for (User user : userList) {
            String userTags = user.getTags();
            List<String> tagsList = gson.fromJson(userTags, new TypeToken<List>() {
            }.getType());
            // 筛选掉空的标签和自己的标签
            long userId = user.getId();
            if (userId == loginUserId || (CollectionUtils.isEmpty(tagsList))) {
                continue;
            }
            // 不是就进行心动匹配
            Long distance = Long.valueOf(AlgorithmUtils.minDistance(LoginTags, tagsList));
            userMatchList.add(new Pair<>(user, distance));
        }
        // 最终比较出来结果 匿名函数a,b 代表两个不同的Pair对象
      return  userMatchList.stream().sorted((a, b) ->
                (int) (a.getValue() - b.getValue())
        ).collect(Collectors.toList());

    }

    /**
     *  根据分页 获取全部用户
     *
     * @param currentPage
     */
    private Page<User> getAllUser(long currentPage) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Page<User> page = this.page(new Page<>(currentPage, PAGE_SIZE), wrapper);
        return page;
    }

    private Page<User> getUserNameLike(long currentPage, String username, User logUser) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 根据用户名进行匹配
        wrapper.like(User::getUsername, username);
        Page<User> userPageSize = this.page(new Page<User>(currentPage, PAGE_SIZE), wrapper);
        return userPageSize;
    }

//    private Page<User> getUsers(long num, User logUser, List<User> userList, String tags) {
//        Gson gson = new Gson();
//        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
//        }.getType());
//        List<User> users = null;
//        if (CollectionUtils.isEmpty(tagList)) {
//            int i = 0;
//            for (User user : userList) {
//                if (user.getId() == logUser.getId()) {
//                    userList.remove(i);
//                    break;
//                }
//                i++;
//            }
//            users = userList;
//            return null;
//        }
//        // 用户列表的下标 => 相似度
//        // pair保存的是一对key value，而map可以保存多对key value。
//        // 即:pair => (1,3)    map => (1,3),(2,3),(3,3)
//        List<Pair<User, Long>> list = new ArrayList<>();
//        // 依次计算所有用户和当前用户的相似度
//        for (int i = 0; i < userList.size(); i++) {
//            User user = userList.get(i);
//            String userTags = user.getTags();
//            // 无标签或者标签为当前用户
//            if (StringUtils.isBlank(userTags) || user.getId() == logUser.getId()) {
//                continue;
//            }
//            List<String> usertagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
//            }.getType());
//            // 计算分数 分数越小表明契合度越高 =>3, 4  表明3的代表的tags匹配契合度更高
//            long distance = AlgorithmUtils.minDistance(tagList, usertagList);
//            // 相当于计算所有用户的tags相似度
//            list.add(new Pair<>(user, distance));
//        }
//
//        // 按编辑距离由小到大排序
//        List<Pair<User, Long>> topUserrPairList = list.stream()
//                .sorted((a, b) -> (int) (a.getValue() - b.getValue())) // .sorted表示根据值进行排序
//                .limit(num)
//                .collect(Collectors.toList());
//        List<User> userVOlist = topUserrPairList.stream()
//                .map(Pair::getKey)       // map是用来进行类型转换的
//                .collect(Collectors.toList());
//        // 用户脱敏
//        users = userVOlist.stream()
//                .map(this::getSaftyUser)
//                .collect(Collectors.toList());
//
//        // 鱼皮的比较复杂:
//       /* // 原本编辑顺序的 userId列表
//        List<Long> userVOlist = topUserrPairList.stream()
//                .map(pair -> pair.getKey().getId())       // map是用来进行类型转换的
//                .collect(Collectors.toList());
//        // 用户脱敏
//        wrapper = new QueryWrapper<>();
//        // 根据userid查询出来所有的用户
//        wrapper.in("id", userVOlist);
////        user -> getSaftyUser(user)
//        Map<Long, List<User>> longListMap = this.list(wrapper)
//                .stream()
//                .map(this::getSaftyUser)
//                .collect(Collectors.groupingBy(User::getId));
//        List<User> FinalUserList = new ArrayList<>();
//        // 作用是将排序好的 userVolist 取出来重新放入一个新的集合里面
//        for (Long userId : userVOlist) {
//            FinalUserList.add(longListMap.get(userId).get(0));
//        }*/
//        return users;
//    }

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
     *
     * @param phone
     * @param
     * @param password
     * @param confirmPassword
     */
    @Override
    public void updatePassword(String phone, String password, String confirmPassword, HttpServletRequest request) {
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
        logUser.setUserPassword(encryptPassword);
        this.updateById(logUser);
        // 用户注销
        int i = userLogout(request);
        if (i < 0) {
            throw new BusinessEception(ErrorCode.SYSTEM, "系统异常");
        }

    }

    /**
     * 修改手机号
     * @param phone
     * @param newPhone
     * @param request
     */
    @Override
    public void updatePhone(String phone, String newPhone, HttpServletRequest request) {
        if (phone.equals(newPhone)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "修改手机号与原来一致");
        }
        User logUser = this.getLogUser(request);
        logUser.setPhone(newPhone);
        //  获取此次对话，并修改保存到对话的对象信息 => 修改当前手机号信息
        request.getSession().setAttribute(LOGIN_USER_KEY,logUser);
        boolean flag = this.updateById(logUser);
        if (!flag) {
            throw new BusinessEception(ErrorCode.SYSTEM, "系统异常");
        }

    }

    /**
     * 获取私聊对象信息
     *
     * @param toId 私聊用户的id
     * @param userId 登录用户的id
     * @return
     */
    @Override
    public UserVO getUserById(Long toId, long userId) {
        // 获取私聊对象，托敏返回
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        // 获取是否关注
        wrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, toId);
        User toUser = this.getById(toId);
        UserVO userVO = new UserVO();
        // 脱敏拷贝
        BeanUtils.copyProperties(toUser, userVO);
        // 获取是否有关注的人
        int count = followService.count(wrapper);
        userVO.setIsFollow(count > 0);
        return userVO;
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




