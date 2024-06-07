package xu.yuan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import xu.yuan.Common.Result;
import xu.yuan.Common.ResultUtils;
import xu.yuan.Eception.BusinessEception;
import xu.yuan.enums.ErrorCode;
import xu.yuan.model.domain.User;
import xu.yuan.model.request.ChatRequest;
import xu.yuan.model.vo.ChatMessageVO;
import xu.yuan.model.vo.PrivateChatVO;
import xu.yuan.service.ChatService;
import xu.yuan.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static xu.yuan.Constant.ChatConstant.*;

/**
 * 聊天控制器
 *
 * @author OchiaMalu
 * @date 2023/06/19
 */
@RestController
@RequestMapping("/chat")
@Api(tags = "聊天管理模块")
public class ChatController {

    /**
     * 聊天服务 每个模块对应一种功能和API
     */
    @Resource
    private ChatService chatService;

    /**
     * 用户服务
     */
    @Resource
    private UserService userService;


    /**
     * 进行大厅聊天
     *
     */
    @GetMapping("/hallChat")
    @ApiOperation(value = "获取大厅聊天")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<ChatMessageVO>> getHallChat(HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        List<ChatMessageVO> hallChat = chatService.getHallChat(HALL_CHAT, loginUser);
        return ResultUtils.success(hallChat);
    }

    /**
     * 进行队伍聊天
     *
     * @param request 请求
     * @param chatRequest 队伍id获取
     * @return {@link }<{@link List}<{@link }>>
     */
    @PostMapping("/teamChat")
    @ApiOperation(value = "获取大厅聊天")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<ChatMessageVO>> getTeamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessEception(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        List<ChatMessageVO> teamChat = chatService.getTeamChat(TEAM_CHAT, loginUser,chatRequest);
        return ResultUtils.success(teamChat);
    }



    /**
     * 私聊
     *
     * @param chatRequest 聊天请求
     * @param request     请求
     */
    @PostMapping("/privateChat")
    @ApiOperation(value = "获取私聊")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "chatRequest",
                    value = "聊天请求"),
                    @ApiImplicitParam(name = "request",
                            value = "request请求")})
    public Result<List<ChatMessageVO>> getPrivateChat(@RequestBody ChatRequest chatRequest,
                                                            HttpServletRequest request) {

        if (chatRequest == null) {
            throw new BusinessEception(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        // 获取私聊信息
        List<ChatMessageVO> privateChat = chatService.getPrivateChat(chatRequest, PRIVATE_CHAT, loginUser);
        return ResultUtils.success(privateChat);
    }

    /**
     * 获取私聊用户表信息
     * @param request
     * @return
     */
    @GetMapping("/private")
    @ApiOperation(value = "获取私聊列表")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public Result<List<PrivateChatVO>> getPrivateChatList(HttpServletRequest request) {
        User loginUser = userService.getLogUser(request);
        if (loginUser == null) {
            throw new BusinessEception(ErrorCode.NOT_LOGIN);
        }
        List<PrivateChatVO> userList = chatService.getPrivateList(loginUser.getId());
        return ResultUtils.success(userList);
    }

}
