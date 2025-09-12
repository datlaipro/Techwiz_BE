package tmtd.event.notifications;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tmtd.event.registrations.events.RegistrationSucceeded;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = false)
public class RegistrationEmailListener {

    // TRỌNG TÂM: dùng interface
    private final RegistrationEmailPort email;

    @TransactionalEventListener
    public void on(RegistrationSucceeded e) {
        // Lấy các tham số từ event của bạn
        email.sendRegistrationSuccess(
            e.studentEmail(), e.eventTitle(), e.eventId(), e.registrationId()
        );
    }
}
