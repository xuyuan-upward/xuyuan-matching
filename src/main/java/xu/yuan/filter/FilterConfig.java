//package xu.yuan.filter;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.AntPathMatcher;
//import xu.yuan.Common.Result;
//import xu.yuan.Common.ResultUtils;
//import xu.yuan.Eception.BusinessEception;
//import xu.yuan.enums.ErrorCode;
//import xu.yuan.model.domain.User;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import static xu.yuan.Constant.UserConstant.USER_LOGIN_STATE;
//@Slf4j
////@Configuration
//public class FilterConfig implements Filter {
//
//    //路径匹配器，支持通配符
//    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        HttpServletResponse response = (HttpServletResponse) servletResponse;
//
//        //获取请求的url
//        String requestURI = request.getRequestURI();
//
//        //判断哪些放行
//        String[] PermissionUrls = new String[]{
//                "/api/user/login", "/api/user/register",
//                "/api/user/message",
//                "/api/user/loginOut",
//                "/api/user/recommend",
//                "/api/user/search/tags",
//                "/api/doc.html",
//        };
//        //某些路径直接放行，不需要判断是否登录
//        boolean check = check(PermissionUrls, requestURI);
//        if (check) {
//            // 放行拦截的请求
//            log.info("本次{}请求放行:",requestURI);
//            filterChain.doFilter(request, response);
//            return;
//        }
//        // 判断是否登录
//        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
//        // 已经登录
//        if (loginUser != null ){
//            log.info("本次{}请求放行:",requestURI);
//            filterChain.doFilter(request, response);
//            return;
//        }
//        //未登录
//        //未登录返回登录界面，返回数据 JSON.toJSONString(ResultUtils.error(ErrorCode.NOT_LOGIN))
//        log.info("未登录:此次请求路径为：{},{}",requestURI,JSON.toJSONString(ResultUtils.error(ErrorCode.NOT_LOGIN)));
//        response.getWriter().write(JSON.toJSONString(ResultUtils.error(ErrorCode.NOT_LOGIN)));
//    }
//
//    /**
//     * 是否放行
//     *
//     * @param PermissionUrls
//     * @param requestURL
//     * @return
//     */
//    private boolean check(String[] PermissionUrls, String requestURL) {
//        for (String Url : PermissionUrls) {
//            boolean match = PATH_MATCHER.match(Url, requestURL);
//            if (match) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    /**
////     * 将返回json对象给前端
////     * @param response
////     * @param json
////     */
////    private void returnJson(ServletResponse response, String json) {
////        PrintWriter writer = null;
////        response.setCharacterEncoding("UTF-8");
////        response.setContentType("application/json; charset=utf-8");
////        try {
////            writer = response.getWriter();
////            writer.print(json);
////        } catch (IOException e) {
////            log.error("response error", e);
////        } finally {
////            if (writer != null)
////                writer.close();
////        }
////    }
//}
