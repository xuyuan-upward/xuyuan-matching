package xu.yuan.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
/**
 * 包装后端每个队伍的信息
 */
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = 3889051801937936429L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 返回表信息和创建队伍用户的信息
     */
  private UserVO createUser;

    /**
     * 是否已经加入队伍
     */

    private boolean hasJoin = false;

    /**
     * 已经加入的用户数
     */

    private int hasJoinNum ;
}
