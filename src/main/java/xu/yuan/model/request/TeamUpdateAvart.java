package xu.yuan.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 团队封面更新请求
 *
 * @author OchiaMalu
 * @date 2024/01/25
 */
@Data
public class TeamUpdateAvart {
    /**
     * 队伍id
     */
    private Long id;
    /**
     * 图片文件
     */
    private MultipartFile file;
}
