package xu.yuan.Common;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class DeleteRequest extends PageRequest {
/**
 * 队伍id
 */
private long id;

}