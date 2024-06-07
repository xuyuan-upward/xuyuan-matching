package xu.yuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 最大人数
     */
    @TableField(value = "maxNum")
    private Integer maxnum;

    /**
     * 过期时间
     */
    @TableField(value = "expireTime")
    private Date expiretime;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userid;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createtime;

    /**
     * 
     */
    @TableField(value = "updateTime")
    private Date updatetime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Integer isdelete;

    /**
     * 队伍图片
     */
    @TableField(value = "coverImage")
    private String coverimage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 队伍名称
     */
    public String getName() {
        return name;
    }

    /**
     * 队伍名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 最大人数
     */
    public Integer getMaxnum() {
        return maxnum;
    }

    /**
     * 最大人数
     */
    public void setMaxnum(Integer maxnum) {
        this.maxnum = maxnum;
    }

    /**
     * 过期时间
     */
    public Date getExpiretime() {
        return expiretime;
    }

    /**
     * 过期时间
     */
    public void setExpiretime(Date expiretime) {
        this.expiretime = expiretime;
    }

    /**
     * 用户id
     */
    public Long getUserid() {
        return userid;
    }

    /**
     * 用户id
     */
    public void setUserid(Long userid) {
        this.userid = userid;
    }

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 创建时间
     */
    public Date getCreatetime() {
        return createtime;
    }

    /**
     * 创建时间
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
     * 是否删除
     */
    public Integer getIsdelete() {
        return isdelete;
    }

    /**
     * 是否删除
     */
    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

    /**
     * 队伍图片
     */
    public String getCoverimage() {
        return coverimage;
    }

    /**
     * 队伍图片
     */
    public void setCoverimage(String coverimage) {
        this.coverimage = coverimage;
    }
}