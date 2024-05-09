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
    private String useraccount;

    /**
     * 
     */
    private String avatarurl;

    /**
     * 
     */
    private Integer gender;

    /**
     * 
     */
    private String userpassword;

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
    private Integer userstatus;

    /**
     * 
     */
    private Date createtime;

    /**
     * 
     */
    private Date updatetime;

    /**
     * 0---->表示删除

     */
    @TableLogic
    private Integer isdelete;

    /**
     * 
     */
    private Integer role;

    /**
     * 星球编号
     */
    private String planetcode;

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