package xu.yuan.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.properties.SMSProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * smsutil
 * 短信发送工具
 *
 * @author xuyuan
 */

@Log4j2
@Component
public class SMSUtils {

    private static SMSProperties smsProperties;

    @Resource
    private SMSProperties tempProperties;

    /**
     * 发送消息
     *
     * @param phoneNum 电话号码
     * @param code     密码
     */
    public static void sendMessage(String phoneNum, String code) {
        IClientProfile profile = DefaultProfile.getProfile(
                smsProperties.getRegionId(),
                smsProperties.getAccessKey(),
                smsProperties.getSecretKey()
        );
        System.out.println(smsProperties);
        IAcsClient client = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNum);
        String signName = smsProperties.getSignName();
        request.setSignName(smsProperties.getSignName());
        String templateCode = smsProperties.getTemplateCode();
        request.setTemplateCode(smsProperties.getTemplateCode());
        request.setTemplateParam("{\"code\":\""+code+"\"}");
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            // 这个用来标识
            log.info("发送结果: " + response.getMessage());
        } catch (ClientException e) {
            throw new BusinessEception(ErrorCode.SYSTEM,"系统异常");
        }
    }

    /**
     * init属性 它用于标记在bean的生命周期中需要在所有依赖注入完成后立即执行的方法。
     */
    @PostConstruct
    public void initProperties() {
        smsProperties = tempProperties;
    }
}
