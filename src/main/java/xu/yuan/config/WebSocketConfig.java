package xu.yuan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置
 * 其实是创建这个配置类去扫描含有@ServerEndpoint注解
 * 通过在配置类中声明一个ServerEndpointExporter bean，Spring Boot
 * 应用将在启动时自动扫描所有的@ServerEndpoint注解，
 * 并将它们注册为有效的WebSocket端点。
 * 这意味着你无需在Servlet容器中手动配置WebSocket端点，
 * Spring会帮你完成这项工作。
 */
@Configuration
public class WebSocketConfig {
    /**
     * 服务器端点
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     *
     * @return {@link ServerEndpointExporter}
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
