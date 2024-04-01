package xu.yuan.model;

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
public class User implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

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
    @TableLogic //虚拟删除
    private Integer isdelete;

    private Integer role;
    /**
     *
     */


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     */
    public String getUseraccount() {
        return useraccount;
    }

    /**
     *
     */
    public void setUseraccount(String useraccount) {
        this.useraccount = useraccount;
    }

    /**
     *
     */
    public String getAvatarurl() {
        return avatarurl;
    }

    /**
     *
     */
    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    /**
     *
     */
    public Integer getGender() {
        return gender;
    }

    /**
     *
     */
    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /**
     *
     */
    public String getUserpassword() {
        return userpassword;
    }

    /**
     *
     */
    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }

    /**
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     *
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 0--->表示正常状态
     */
    public Integer getUserstatus() {
        return userstatus;
    }

    /**
     * 0--->表示正常状态
     */
    public void setUserstatus(Integer userstatus) {
        this.userstatus = userstatus;
    }

    /**
     *
     */
    public Date getCreatetime() {
        return createtime;
    }

    /**
     *
     */
    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    /**
     *
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     *
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * 0---->表示删除

     */
    public Integer getIsdelete() {
        return isdelete;
    }

    /**
     * 0---->表示删除

     */
    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", username=").append(username);
        sb.append(", useraccount=").append(useraccount);
        sb.append(", avatarurl=").append(avatarurl);
        sb.append(", gender=").append(gender);
        sb.append(", userpassword=").append(userpassword);
        sb.append(", phone=").append(phone);
        sb.append(", email=").append(email);
        sb.append(", userstatus=").append(userstatus);
        sb.append(", createtime=").append(createtime);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", isdelete=").append(isdelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}