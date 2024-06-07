package xu.yuan.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私聊返回
 *
 * @author OchiaMalu
 * @date 2024/05/23
 */
@Data
@ApiModel(value = "私聊返回")
public class PrivateChatVO implements Serializable, Comparable<PrivateChatVO> {

    private static final long serialVersionUID = -3426382762617526337L;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户Id")
    private Long userId;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String username;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String avatarUrl;

    /**
     * 最后一条消息
     */
    @ApiModelProperty(value = "最后消息")
    private String lastMessage;

    /**
     * 最后一条消息日期
     */
    @ApiModelProperty(value = "最后消息日期")
    private String lastMessageDate;

    /**
     * 未读消息数量
     */
    @ApiModelProperty(value = "未读消息数量")
    private Integer unReadNum;


    /**
     * 个compareTo方法的实现确保了当按照某个集合（如List<PrivateChatVO>）的自然顺序排序时，
     * 最后消息日期较新的PrivateChatVO对象将排在较旧的对象前面。这是一种降序排序逻辑。
     * @param other
     * @return
     */
    @Override
    public int compareTo(PrivateChatVO other) {
        // 越早的信息排前面 但是有符号导致最新的消息排前面
        return -this.getLastMessageDate().compareTo(other.getLastMessageDate());
    }
}
