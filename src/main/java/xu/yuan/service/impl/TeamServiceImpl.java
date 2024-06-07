package xu.yuan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.TeamStatusEnum;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.model.request.*;
import xu.yuan.model.vo.TeamUserVo;
import xu.yuan.model.vo.TeamVO;
import xu.yuan.model.vo.UserVO;
import xu.yuan.service.TeamService;
import xu.yuan.mapper.TeamMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.UserService;
import xu.yuan.service.UserTeamService;
import xu.yuan.utils.AliOSSUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static xu.yuan.Common.SystemCommon.MAXIMUM_JOINED_USER_AVATAR_NUM;
import static xu.yuan.Common.SystemCommon.PAGE_SIZE;
import static xu.yuan.Constant.RedisConstants.TEAM_LOCK_KEY;

/**
 * @author 许苑
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-04-29 11:07:17
 */
@Service
@Transactional
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
@Resource
private AliOSSUtils aliOSSUtils;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        //2. 是否登录，未登录不允许创建(全局拦截处理器已经做了)
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        int maxNum = team.getMaxnum();
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (description.length() > 512 || StringUtils.isBlank(description)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (teamStatusEnum == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (teamStatusEnum.equals(TeamStatusEnum.SECRETE)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessEception(ErrorCode.PARAMS_ERROR, "密码参数错误");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpiretime();
        if (new Date().after(expireTime)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        //获取当前登录用户的id
        long userId = loginUser.getId();
        // todo 有bug 多线程下会导致同时创建100个队伍
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getUserid, userId);
        long count = this.count(wrapper);
        if (count >= 5) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserid(userId);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessEception(ErrorCode.SYSTEM, "插入队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        Long teamId = team.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setId(0L);
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessEception(ErrorCode.SYSTEM, "插入队伍失败");
        }
        return teamId;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        // 请求参数的判定
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        // 获取队伍信息
        Team oldTeam = this.getById(id);
        // 判断传入的信息是否存在
        if (oldTeam == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        // 判断是否为加密,加密需要输入加密密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRETE)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessEception(ErrorCode.PARAMS_ERROR, "加密队伍必须设置有密码");
            }
        }
        // 只有管理者或者创建者可以修改
        // 既不是管理者也不是创建者,不可以修改
        if (oldTeam.getUserid() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessEception(ErrorCode.NO_AUTH);
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        boolean flag = this.updateById(updateTeam);
        // 这个修改不会拼接全部的 Team属性值而只拼接set有的
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {

        // 判断用户是否输入队伍id是否有误
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        Integer status = team.getStatus();
        if (team == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 过期的队伍不能加入
        Date expireTime = team.getExpiretime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessEception(ErrorCode.NULL_ERROR, "队伍已经过期");
        }
        //私密的队伍不能加入
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessEception(ErrorCode.NO_AUTH, "禁止加入私密的队伍");
        }
        // 加密的队伍匹配密码是否对应
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRETE.equals(teamStatusEnum)) {
            boolean falg = !StringUtils.isNotBlank(password);
            boolean equals = password.equals(team.getPassword());
            if (falg || !equals) {
                throw new BusinessEception(ErrorCode.NO_AUTH, "密码错误");
            }
        }
        long userId = loginUser.getId();
        String teamIdKey = TEAM_LOCK_KEY + ":" + teamId;
        // 尝试获取队伍锁
        RLock teamlock = redissonClient.getLock(teamIdKey);
        try {
            // 抢到锁并执行
            while (true) {
                if (teamlock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock: " + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessEception(ErrorCode.PARAMS_ERROR, "队伍已经满了");
                    }
                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();

                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeam(teamId);
                    if (teamHasJoinNum >= team.getMaxnum()) {
                        throw new BusinessEception(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (teamlock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                teamlock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User logUser) {
        Long teamId = teamQuitRequest.getTeamId();
        Team team = this.getTeam(teamId);
        long userId = logUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        // 判断用户是否已经加入该队伍
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).eq("teamId", teamId);
        int count = userTeamService.count(wrapper);
        if (count == 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        // 获取当前队人数
        int countTeam = this.countTeam(teamId);
        // 队伍还剩一人时候
        if (countTeam == 1) {
            // 删除队伍和所有加入队伍的关系
            this.removeById(teamId);

        } else {
            // 是队长
            if (team.getUserid() == userId) {
                // 把最近的队长给最早加入的用户
                // 查询已加入队伍的所有用户和时间 按userteam关系表,越早加入的用户userteam越小
                wrapper = new QueryWrapper<>();
                wrapper.eq("teamId", teamId);
                wrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(wrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessEception(ErrorCode.SYSTEM);
                }
                UserTeam nextUser = userTeamList.get(1);
                Long nextUserUserId = nextUser.getUserId();
                // 更新team的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserid(nextUserUserId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessEception(ErrorCode.SYSTEM);
                }
            }
            //不是队长直接移除
        }
        // 移除关系
        return userTeamService.remove(wrapper);
    }

    /**
     * 队长解散队伍
     *
     * @param teamId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        Team team = this.getTeam(teamId);
        // 校验是否是队长
        long userId = loginUser.getId();
        if (team.getUserid() != userId) {
            throw new BusinessEception(ErrorCode.NO_AUTH, "不是队长无权限");
        }
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId);
        UserTeam userTeam = userTeamService.getOne(wrapper);
        log.info("查询出来的队伍是: {}", userTeam);
        boolean remove = userTeamService.remove(wrapper);
        if (!remove) {
            throw new BusinessEception(ErrorCode.SYSTEM, "删除队伍关联信息失败");
        }
        // 删除队伍
        boolean deleteTeam = this.removeById(teamId);
        if (!deleteTeam) {
            throw new BusinessEception(ErrorCode.SYSTEM, "删除队伍信息失败");
        }
        return deleteTeam;
    }

    /**
     * 获取我所有参加入的队伍
     *
     * @param userId 当前用户的id
     * @return
     */
    @Override
    public List<TeamUserVo> listAllMyJoin(long userId) {
        //根据id得出用户参加的队伍user_team 中的teamid
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeam::getUserId, userId);
        // map就是将每个对象转换成对应引用方法的返回值
        List<Long> teamIds = userTeamService.list(wrapper).stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        // 如果不存在则报错
        if (teamIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Team> teams = this.listByIds(teamIds);
        return teams.stream().map((team) -> {
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            teamUserVo.setHasJoin(true);
            return teamUserVo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取队伍信息
     *
     * @param currentPage
     * @param teamRequst
     * @param admin
     * @return
     */
    @Override
    public Page<TeamVO> listTeams(long currentPage, TeamRequst teamRequst, boolean admin) {
        // 组合查询
        // 获取当前搜索词语
        String searchText = teamRequst.getSearchText();
        // 根据队伍Id来查询
        Long teamId = teamRequst.getId();
        // 根据队伍名称来查询
        String teamName = teamRequst.getName();
        // 根据队伍描述来查询
        String description = teamRequst.getDescription();
        // 根据状态来查询
        Integer status = teamRequst.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (teamStatusEnum == null) {
            teamStatusEnum = TeamStatusEnum.PUBLIC;
        }
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        // 根据条件来进行查询
        // sql：=> where (teamName like( searchText) or description like( searchText))
        //               and (statsu = teamStatusEnum.getstatus);
        wrapper.and(StringUtils.isNotBlank(searchText),
                qw -> qw.like(Team::getName, searchText)
                        .or()
                        .like(Team::getDescription, description))
                .eq(Team::getStatus, teamStatusEnum.getStatus())
                // 表示不展示过期的队伍
                .and(qw -> qw.gt(Team::getExpiretime, new Date()).or().isNull(Team::getExpiretime))
                .orderBy(true, false, Team::getCreatetime);
        Page<TeamVO> teamVOPage = listPageTeam(currentPage, wrapper);
        return teamVOPage;
    }

    /**
     * 获取队伍者加入照片
     *
     * @param teamVoPage
     * @return
     */
    @Override
    public Page<TeamVO> getJoinedUserAvatarUrl(Page<TeamVO> teamVoPage) {
        teamVoPage.getRecords().forEach((item) -> {
            Long teamId = item.getId();
            LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserTeam::getTeamId, teamId);
            // 获取加入队伍者Id 其中包括队长进去
            List<Long> joinedUserIdList = userTeamService.list(wrapper)
                    .stream().map((UserTeam::getUserId))
                    .limit(MAXIMUM_JOINED_USER_AVATAR_NUM)
                    .collect(Collectors.toList());
            // 根据id查询 所有用户
//           方法一：  LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//            wrapper.select(User::getAvatarUrl).in(User::getId,joinedUserIdList);
//            List<User> list = userService.list(wrapper);
            List<String> joinedUserAvatarList = userService.listByIds(joinedUserIdList)
                    .stream().map((User::getAvatarUrl))
                    .collect(Collectors.toList());
            item.setJoinedUserAvatars(joinedUserAvatarList);
        });
        return teamVoPage;
    }

    /**
     * 根据队伍Id查询队伍信息
     *
     * @param teamId
     * @param userId
     * @return
     */
    @Override
    public TeamVO getTeam(Long teamId, long userId) {
        // 获取队伍
        Team team = this.getById(teamId);
        TeamVO teamVO = new TeamVO();
        BeanUtils.copyProperties(team, teamVO);
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long count = userTeamService.count(userTeamLambdaQueryWrapper);
        // 获取加入人数
        teamVO.setHasJoinNum(count);
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        long userJoin = userTeamService.count(userTeamLambdaQueryWrapper);
        // 获取登录用户是否加入
        teamVO.setHasJoin(userJoin > 0);
        // 获取用户的领导者昵称
        User leader = userService.getById(team.getUserid());
        teamVO.setLeaderName(leader.getUsername());

        return teamVO;
    }

    /**
     * 根据队伍id 获取队员
     * @param teamId 队伍id
     * @param userId 用户id
     * @return
     */
    @Override
    public List<UserVO> getTeamMember(Long teamId, long userId) {
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeam::getTeamId, teamId);
        // 获取所有加入该队伍的用户的id ，并排除当前登录的用户自己
        List<Long> userList = userTeamService.list(wrapper).stream()
                .map(UserTeam::getUserId).filter(
                        id -> !Objects.equals(id, userId)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(userList)) {
           return new ArrayList<>();
        }
        // 获取用户信息
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userList);
        List<User> users = userService.list(userWrapper);

        // 进行获取的对象拷贝
        return  users.stream().map(user ->
                {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user,userVO);
                    return userVO;
                }).collect(Collectors.toList());

    }

    /**
     * 踢出队员
     * @param teamId 队伍id
     * @param KickUserId 队长id
     * @param loginId 登录用户id
     * @param admin
     */
    @Override
    public void kickOut(Long teamId, Long KickUserId, long loginId, boolean admin) {
        // 获取当前队伍关系 还有就是不能自己踢自己
        Team team = this.getTeam(teamId);
        Long teamUserId = team.getUserid();
        // 判断是否是管理员或则是登录用户
        isAuth(teamUserId, loginId, admin);

        if (team.getUserid() == KickUserId) {
            throw new BusinessEception(ErrorCode.NO_AUTH, "不能踢掉自己");
        }
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeam::getTeamId, teamId).eq(UserTeam::getUserId, KickUserId);
        // 移除当前用户
        userTeamService.remove(wrapper);
    }

    /**
     * 判断是否有权限修改
     * @param teamUserId 队长id
     * @param loginId 当前登录用户id
     * @param admin
     */
    private void isAuth(Long teamUserId, Long loginId, boolean admin) {
        if (!admin && !teamUserId.equals(loginId)) {
            throw new BusinessEception(ErrorCode.NO_AUTH, "没有权限");
        }
    }

    /**
     * 更新队伍照片
     * @param teamUpdateAvart  接收信息
     * @param loginId 当前登录用户id
     * @param admin 是否管理员
     */
    @Override
    public void changeCoverImage(TeamUpdateAvart teamUpdateAvart, long loginId, boolean admin) {

        MultipartFile image = teamUpdateAvart.getFile();
        if (image == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否有权限修改
        Long teamId = teamUpdateAvart.getId();
        Team team = this.getTeam(teamId);
        Long teamUserId = team.getUserid();
        isAuth(teamUserId, loginId, admin);
        //有权修改 上传照片到阿里云
        String imageName = "";
        try {
            imageName  = aliOSSUtils.upload(image);
            if (StringUtils.isBlank(imageName)) {
                throw  new BusinessEception(ErrorCode.SYSTEM);
            }
        } catch (IOException e) {
            throw  new BusinessEception(ErrorCode.SYSTEM);
        }
        // 并修改数据库里面的信息
        team.setCoverimage(imageName);
        team.setId(teamId);
      this.updateById(team);
    }


    /**
     * 获取队长的信息 包括整个队伍的信息
     */
    private Page<TeamVO> listPageTeam(long currentaPage, LambdaQueryWrapper<Team> wrapper) {
        Page<Team> teamPage = this.page(new Page<>(currentaPage, PAGE_SIZE), wrapper);
        if (CollectionUtils.isEmpty(teamPage.getRecords())) {
            return new Page<>();
        }
        Page<TeamVO> teamVOPage = new Page<>();
        // 关联查询创建人的用户信息
        BeanUtils.copyProperties(teamPage, teamVOPage, "records");
        List<Team> teamPageRecords = teamPage.getRecords();
        ArrayList<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamPageRecords) {
            Long userId = team.getUserid();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamVO teamUserVO = new TeamVO();
            // 拷贝队伍信息，给VO
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        teamVOPage.setRecords(teamUserVOList);
        return teamVOPage;
    }


    /**
     * 获取当前队伍的人数
     */
    private int countTeam(long teamId) {
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId);
        return userTeamService.count(wrapper);
    }

    /**
     * 根据team的字段id ,获取当前队伍
     *
     * @param teamId
     * @return
     */
    private Team getTeam(long teamId) {
        if (teamId <= 0) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

}




