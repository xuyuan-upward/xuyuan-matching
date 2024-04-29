package xu.yuan.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xu.yuan.model.domain.User;

@SpringBootTest
@Slf4j
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("xuyuan");
        user.setUseraccount("xuyuannihao");
        user.setAvatarurl("23423423424");
        user.setGender(0);
        user.setUserpassword("1234");
        user.setPhone("18999");
        user.setEmail("234");
        userService.save(user);
        System.out.println(user);
    }

    @Test
    void registerUser() {


        System.out.println();
        System.out.println();
        System.out.println();
    }
}