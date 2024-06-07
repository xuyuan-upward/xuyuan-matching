package xu.yuan.model.request;

import lombok.Data;

@Data
public class TeamKickOutRequest {
    /*   teamId: props.team.id,
        userId: userId*/
    /**
     * 队伍的Id
     */
    private Long  teamId;
    /**
     * 踢出队员的id
     */
    private Long  userId;
}
