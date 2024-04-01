package xu.yuan.model;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户注册请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    private  String userAccount;
    private  String userPassword;

}
