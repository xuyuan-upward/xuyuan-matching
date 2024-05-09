package xu.yuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.TeamStatusEnum;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.model.dto.TeamQuery;
import xu.yuan.model.request.TeamJoinRequest;
import xu.yuan.model.request.TeamQuitRequest;
import xu.yuan.model.request.TeamUpdateRequest;
import xu.yuan.model.vo.TeamUserVo;
import xu.yuan.model.vo.UserVO;
import xu.yuan.service.TeamService;
import xu.yuan.mapper.TeamMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.UserService;
import xu.yuan.service.UserTeamService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static xu.yuan.Constant.JoinTeamConstant.*;

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
    private Redisson redisson;

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
        int maxNum = team.getMaxNum();
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
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        //获取当前登录用户的id
        long userId = loginUser.getId();
        // todo 有bug 多线程下会导致同时创建100个队伍
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getUserId, userId);
        long count = this.count(wrapper);
        if (count >= 5) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
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

    /**
     * 查询队伍
     *
     * @param teamQuery
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            // 根据id进行查询
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                // sql: WHERE (name LIKE '%searchText%' OR description LIKE '%searchText%')
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据teamId查询
            List<Long> IdList = teamQuery.getTeamId();
            if (!CollectionUtils.isEmpty(IdList)) {
                queryWrapper.in("id", IdList);
            }
            //根据username来查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {

                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {

                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessEception(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getStatus());


        }


        // sql: and (expireTime > localdata时间 or expireTime != null)
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> Teamlist = this.list(queryWrapper);
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(Teamlist)) {
            return new ArrayList<>();
        }
        // 根据创建人的信息查询
        for (Team team : Teamlist) {
            Long userId1 = team.getUserId();
            if (userId1 == null) {
                continue;
            }
            // 用户脱敏
            UserVO userVo = new UserVO();
            User user = userService.getById(userId1);
            if (user != null) {
                BeanUtils.copyProperties(user, userVo);
            }
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            teamUserVo.setCreateUser(userVo);
            teamUserVoList.add(teamUserVo);
        }

        return teamUserVoList;
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
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
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
        Date expireTime = team.getExpireTime();
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
            if ( falg || !equals) {
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
                    if (teamHasJoinNum >= team.getMaxNum()) {
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
            if (team.getUserId() == userId) {
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
                updateTeam.setUserId(nextUserUserId);
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
        if (team.getUserId() != userId) {
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




