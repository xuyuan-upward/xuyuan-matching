package xu.yuan.one;

import org.springframework.stereotype.Component;
import xu.yuan.mapper.UserMapper;
import xu.yuan.model.domain.User;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入
     */
//    @Scheduled
    public void doInsertUsers(){
        User user = new User();
        user.setUsername("假许苑");
        user.setUserAccount("fakexuyuan");
        user.setAvatarUrl("https://tse4-mm.cn.bing.net/th/id/OIP-C.DrGC1BblAyZ5C3SxbqFYCwAAAA?w=199&h=199&c=7&r=0&o=5&dpr=2&pid=1.7");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("18778959139");
        user.setEmail("2517115657@qq.com");
        user.setUserStatus(0);
        user.setRole(0);


        userMapper.insert(user);


    }



}
