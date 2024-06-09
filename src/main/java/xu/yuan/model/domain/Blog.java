package xu.yuan.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName blog
 */
@TableName(value ="blog")
public class Blog implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 博文创建者id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 图片，最多5张，多张以","隔开
     */
    @TableField(value = "images")
    private String images;

    /**
     * 文章
     */
    @TableField(value = "content")
    private String content;

    /**
     * 点赞数量
     */
    @TableField(value = "liked_num")
    private Integer likedNum;

    /**
     * 评论数量
     */
    @TableField(value = "comments_num")
    private Integer commentsNum;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableField(value = "is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 图片，最多9张，多张以","隔开
     */
    public String getImages() {
        return images;
    }

    /**
     * 图片，最多9张，多张以","隔开
     */
    public void setImages(String images) {
        this.images = images;
    }

    /**
     * 文章
     */
    public String getContent() {
        return content;
    }

    /**
     * 文章
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 点赞数量
     */
    public Integer getLikedNum() {
        return likedNum;
    }

    /**
     * 点赞数量
     */
    public void setLikedNum(Integer likedNum) {
        this.likedNum = likedNum;
    }

    /**
     * 评论数量
     */
    public Integer getCommentsNum() {
        return commentsNum;
    }

    /**
     * 评论数量
     */
    public void setCommentsNum(Integer commentsNum) {
        this.commentsNum = commentsNum;
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
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 逻辑删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 逻辑删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}