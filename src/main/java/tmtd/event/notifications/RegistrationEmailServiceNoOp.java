
// package tmtd.event.notifications;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
// import org.springframework.stereotype.Service;

// /**
//  * No-Op fallback: Tự động đăng ký khi KHÔNG có bean nào implement RegistrationEmailPort.
//  * Dùng để dev/test mà không cấu hình SMTP -> app vẫn boot bình thường.
//  */
// @Service
// @ConditionalOnMissingBean(RegistrationEmailPort.class) // ✅ fallback mặc định
// public class RegistrationEmailServiceNoOp implements RegistrationEmailPort {

//     private static final Logger log = LoggerFactory.getLogger(RegistrationEmailServiceNoOp.class);

//     @Override
//     public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
//         log.info("[NO-OP] Mail sending disabled or missing implementation. Skip sending to {}", to);
//     }
// }


package tmtd.event.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NO-OP: dùng khi không có impl gửi mail thật.
 * Không gắn @Service/@Component — bean được tạo trong RegistrationEmailAutoConfig.
 */
public class RegistrationEmailServiceNoOp implements RegistrationEmailPort {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEmailServiceNoOp.class);

    @Override
    public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
        log.info("[NO-OP] Mail sending disabled/missing. Skip sending to {}", to);
    }
}
