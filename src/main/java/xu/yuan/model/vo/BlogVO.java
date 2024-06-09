package xu.yuan.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xu.yuan.model.domain.Blog;

import java.io.Serializable;

    /**
     * 博客vo
     *
     * @author xuyuan
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @ApiModel(value = "博文返回")
    public class BlogVO extends Blog implements Serializable {

        /**
         * uid
         */
        private static final long serialVersionUID = -1396990280493874426L;
        /**
         * 显示自己是否给别人点赞
         */
        @ApiModelProperty(value = "是否点赞")
        private Boolean isLike;
        /**
         * 封面图片
         */
        @ApiModelProperty(value = "封面图片")
        private String coverImage;
        /**
         * 作者
         */
        @ApiModelProperty(value = "作者")
        private UserVO author;
    }