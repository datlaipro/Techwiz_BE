package tmtd.event.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // endpoint handshake
        .setAllowedOriginPatterns("*") // chỉnh CORS theo domain FE
        .withSockJS(); // tùy chọn
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic"); // kênh pub/sub
    registry.setApplicationDestinationPrefixes("/app");
  }
}
