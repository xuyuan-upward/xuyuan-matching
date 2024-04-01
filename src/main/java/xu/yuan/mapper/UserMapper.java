package xu.yuan.mapper;

import org.apache.ibatis.annotations.Mapper;
import xu.yuan.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 肖广龙
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-03-27 16:01:57
* @Entity xu.yuan.model.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




