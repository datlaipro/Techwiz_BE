package tmtd.event.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(RegistrationEmailPort.class)
// Nếu chưa có bản "thật" thì dùng No-Op
public class RegistrationEmailServiceNoOp implements RegistrationEmailPort {
    @Override
    public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
        // Không gửi gì cả; có thể log nếu muốn
        // log.info("Mail disabled; skip sending to {}", to);
    }
}
