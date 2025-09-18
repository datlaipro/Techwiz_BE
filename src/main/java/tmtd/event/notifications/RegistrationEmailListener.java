
// package tmtd.event.notifications;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.event.TransactionPhase;
// import org.springframework.transaction.event.TransactionalEventListener;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import tmtd.event.registrations.events.RegistrationSucceeded;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// @ConditionalOnBean(RegistrationEmailPort.class) // ✅ chỉ tạo khi đã có Port
// public class RegistrationEmailListener {

//     private final RegistrationEmailPort email; // có thể là impl thật hoặc No-Op

//     @Async("emailExecutor")
//     @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//     public void on(RegistrationSucceeded e) {
//         final String to = e.studentEmail();
//         if (to == null || to.isBlank()) {
//             log.warn("Skip sending registration email: empty recipient (eventId={}, regId={})",
//                     e.eventId(), e.registrationId());
//             return;
//         }
//         log.info("Send registration email -> to={}, eventId={}, regId={}", to, e.eventId(), e.registrationId());
//         email.sendRegistrationSuccess(to, e.eventTitle(), e.eventId(), e.registrationId());
//     }
// }



package tmtd.event.notifications;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tmtd.event.registrations.events.RegistrationSucceeded;

/**
 * Lắng nghe sự kiện đã đăng ký thành công và gửi mail.
 * Không gắn @Component — bean được tạo trong RegistrationEmailAutoConfig.
 */
@RequiredArgsConstructor
@Slf4j
public class RegistrationEmailListener {

    private final RegistrationEmailPort email;

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RegistrationSucceeded e) {
        final String to = e.studentEmail();
        if (to == null || to.isBlank()) {
            log.warn("Skip sending registration email: empty recipient (eventId={}, regId={})",
                     e.eventId(), e.registrationId());
            return;
        }
        log.info("Send registration email -> to={}, eventId={}, regId={}", to, e.eventId(), e.registrationId());
        email.sendRegistrationSuccess(to, e.eventTitle(), e.eventId(), e.registrationId());
    }
}
