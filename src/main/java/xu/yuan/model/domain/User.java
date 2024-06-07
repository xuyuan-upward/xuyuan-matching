package xu.yuan.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user
 */
@Data
@TableName(value ="user")
public class User implements Serializable{
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 
     */
    private String username;

    /**
     * 
     */
    private String userAccount;

    /**
     * 
     */
    private String avatarUrl;

    /**
     * 
     */
    private Integer gender;

    /**
     * 
     */
    private String userPassword;

    /**
     * 
     */
    private String phone;

    /**
     * 
     */
    private String email;

    /**
     * 0--->表示正常状态
     */
    private Integer userStatus;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 0---->表示删除

     */
    @TableLogic
    private Integer isDelete;

    /**
     * 
     */
    private Integer role;
    /**
     * 个性签名
     */
    private String personality;

    /**
     * 星球编号
     */
    private int planetCode;

    private String tags;

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}