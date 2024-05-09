package xu.yuan.model.request;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import xu.yuan.Common.PageRequest;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class TeamQuitRequest extends PageRequest {
    /**
     * 队伍的id
     */
    private Long teamId;

}