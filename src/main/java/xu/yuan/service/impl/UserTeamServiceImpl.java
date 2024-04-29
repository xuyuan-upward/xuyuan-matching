package xu.yuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xu.yuan.model.domain.UserTeam;
import xu.yuan.service.UserTeamService;
import xu.yuan.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 许苑
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-04-29 11:09:58
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




