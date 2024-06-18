package xu.yuan.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class BlogUpdateRequest implements Serializable {


    private static final long serialVersionUID = 1135439905423641116L;
    /**
     * 文章作者id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 图片
     */
    @ApiModelProperty(value = "图片")
    private MultipartFile[] images;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 内容
     */
    @ApiModelProperty(value = "正文")
    private String content;

    /**
     * 原来文章的照片
     */
    private String imgStr;
}
