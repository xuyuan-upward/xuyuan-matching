package xu.yuan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {
@Autowired
private AliOSSUtils aliOSSUtils;
    @Autowired
private UserService userService;
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody MultipartFile file,HttpServletRequest request){
        // 返回图片的地址
        String imgURL = "";
        try {
             imgURL = aliOSSUtils.upload(file);
        } catch (IOException e) {
            throw  new BusinessEception(ErrorCode.SYSTEM);
        }
        //  存放到数据库里面
        User logUser = userService.getLogUser(request);
        logUser.setAvatarurl(imgURL);
        userService.updateById(logUser);
        return ResultUtils.success(imgURL);
    }
}
