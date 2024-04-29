package xu.yuan.enums;


/**
 * 队伍状态枚举
 */
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRETE(2,"加密")
    ;
    private int status;
    private String text;


    public static TeamStatusEnum getTeamStatusEnum(Integer value) {
        if (value == null){
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getStatus() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    TeamStatusEnum(int status, String text) {
        this.status = status;
        this.text = text;
    }
}
