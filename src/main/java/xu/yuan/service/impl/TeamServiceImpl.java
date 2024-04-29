package xu.yuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import xu.yuan.enums.ErrorCode;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.TeamStatusEnum;
import xu.yuan.model.domain.Team;
import xu.yuan.model.domain.User;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.service.TeamService;
import xu.yuan.mapper.TeamMapper;
import org.springframework.stereotype.Service;
import xu.yuan.service.UserTeamService;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author 许苑
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-04-29 11:07:17
*/
@Service
@Transactional
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
    @Resource
    private UserTeamService userTeamService;
    @Override
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        //2. 是否登录，未登录不允许创建(全局拦截处理器已经做了)
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        int  maxNum = team.getMaxNum();
        if(maxNum <1 || maxNum >20){
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (description.length() > 512 || StringUtils.isBlank(description)){
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if (teamStatusEnum == null){
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (teamStatusEnum.equals(TeamStatusEnum.SECRETE) ){
            if ( StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessEception(ErrorCode.PARAMS_ERROR,"密码参数错误");
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
        wrapper.eq(Team::getUserId,userId);
        long count = this.count(wrapper);
        if (count >= 5){
            throw new BusinessEception(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
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
}




