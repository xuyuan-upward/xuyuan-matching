package xu.yuan.mapper;

import org.apache.ibatis.annotations.Mapper;
import xu.yuan.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 许苑
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-04-03 15:38:45
* @Entity xu.yuan.model.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




