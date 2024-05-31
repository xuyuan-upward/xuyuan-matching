package xu.yuan.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import xu.yuan.properties.MatchProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 消息utils
 *
 *
 */
@Log4j2
@Component
public class MessageUtils {
   private static MatchProperties matchProperties;
   @Resource
   private MatchProperties tempProperties;


    /**
     * 发送消息
     *
     * @param phoneNum 电话号码
     * @param code     密码
     */
    public static void sendMessage(String phoneNum, String code) {
        if (matchProperties.isUseShortMessagingService()) {
            SMSUtils.sendMessage(phoneNum, code);
        } else {
            log.info("验证码: " + code);
        }
    }

    /**
     * init属性
     */
    @PostConstruct
    public void initProperties() {
        matchProperties = tempProperties;
    }
}
