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
    private Integer maxNum;

    /**
     * 过期时间
     */
    @TableField(value = "expireTime")
    private Date expireTime;

    /**
     * 用户id
     */
    @TableField(value = "userId")
    private Long userId;

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
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;

    /**
     * 队伍图片
     */
    @TableField(value = "coverImage")
    private String coverImage;

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
    public Integer getMaxNum() {
        return maxNum;
    }

    /**
     * 最大人数
     */
    public void setMaxNum(Integer maxNum) {
        this.maxNum = maxNum;
    }

    /**
     * 过期时间
     */
    public Date getExpireTime() {
        return expireTime;
    }

    /**
     * 过期时间
     */
    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * 用户id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 用户id
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * 队伍图片
     */
    public String getCoverImage() {
        return coverImage;
    }

    /**
     * 队伍图片
     */
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
}