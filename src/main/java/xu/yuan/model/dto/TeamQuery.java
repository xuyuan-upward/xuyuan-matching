package xu.yuan.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xu.yuan.Common.PageRequest;

import java.util.Date;
import java.util.List;

/**
 * 队伍
 * @TableName team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageRequest  {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**

     *  获取当前用户所有加入队伍的teamId
     */
    private List<Long> teamId;
    /**
     * 搜索关键词(同时对队伍名称和描述搜索)
     */
    private String searchText;

    /**
     * 搜索队伍名称
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


}