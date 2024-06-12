package xu.yuan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.User;
import xu.yuan.service.UserService;
import xu.yuan.utils.AliOSSUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import static xu.yuan.Constant.RedisConstants.RECOMMAN_LAST_KEY;
import static xu.yuan.Constant.UserConstant.LOGIN_USER_KEY;

@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {
@Autowired
private AliOSSUtils aliOSSUtils;
    @Autowired
private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody MultipartFile file,HttpServletRequest request){
        // 返回图片的地址
        String imgURL = "";
        try {
             imgURL = aliOSSUtils.upload(file);
        } catch (IOException e) {
            throw  new BusinessEception(ErrorCode.SYSTEM);
        }
        // 上传照片的时候同时要跟新当前登录状态下用户的信息
        //  存放到数据库里面

        User logUser = userService.getLogUser(request);
        logUser.setAvatarUrl(imgURL);
        // TODO 上传照片的时候同时要跟新当前登录状态下用户的信息
        //  所谓修改用户信息的时候都需要进行所谓状态信息修改  这个值得考虑
//        request.getSession().setAttribute(LOGIN_USER_KEY,logUser);
        userService.updateById(logUser);
        // 删除redis缓存
        redisTemplate.delete(RECOMMAN_LAST_KEY);
        return ResultUtils.success(imgURL);
    }
}
