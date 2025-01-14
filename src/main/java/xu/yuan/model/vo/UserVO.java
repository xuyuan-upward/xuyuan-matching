package xu.yuan.model.vo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
// VO类型一般是用来返回给前端的数据类型
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表 json
     */
    private String tags;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
    /**
     * 星球编号
     */
    private String planetCode;
    /**
     * 星球编号
     */
    private String personality;

    /**
     * 用户角色： 0 - 普通用户 1 - 管理员
     */
    @ApiModelProperty(value = "用户角色")
    private Integer role;

    /**
     * 是否关注
     */
    @ApiModelProperty(value = "当前登录用户是否关注")
    private Boolean isFollow;
    private static final long serialVersionUID = 1L;
}
