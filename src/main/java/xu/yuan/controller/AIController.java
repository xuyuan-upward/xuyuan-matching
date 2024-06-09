package xu.yuan.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.AIRequest;
import xu.yuan.service.UserService;
import xu.yuan.utils.AIUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * AI 控制器
 *
 * @author xuyuan
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private UserService userService;

    /**
     * 获取 AI 消息
     *
     * @param aiRequest AI请求
     */
    @PostMapping
    @ApiOperation(value = "获取AI消息")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "addCommentRequest", value = "AI请求"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public Result<String> getAIMessage(@RequestBody AIRequest aiRequest, HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        if (aiRequest.getMessage().isEmpty()) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        String aiMessage = AIUtils.getAIMessage(aiRequest.getMessage());
        return ResultUtils.success(aiMessage);
    }
}
