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
public class TeamJoinRequest extends PageRequest {

    /**
     * 队伍id
     */
    private Long teamId;
    /**
     * 队伍密码
     */
    private String password;


}