package xu.yuan.model.request;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xu.yuan.Common.PageRequest;

import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class TeamUpdateRequest extends PageRequest {

    /**
     * id
     */
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
    /**
     * 最大人数
     */
    private Integer maxNum;
    /**
     * 队伍密码
     */
    private String password;
    /**
     * 当前队伍加入人数
     */
    private Integer hasJoinNum;
}