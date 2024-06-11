//package xu.yuan.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import xu.yuan.model.domain.User;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import static xu.yuan.Constant.UserConstant.LOGIN_USER_KEY;
//
//@Slf4j
//@Component
//public class UserInterceptor implements HandlerInterceptor {
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //从session中获取user的信息
//        User loginUser =(User)request.getSession().getAttribute(LOGIN_USER_KEY);
//        //判断用户是否登录
//        if (null == loginUser){
//            response.sendRedirect(request.getContextPath()+"/user/error");
//            log.info("用户未登录");
//            return false;
//        }
//        log.info("用户已经登录");
//        return true;//返回true代表不拦截
//    }
//}
