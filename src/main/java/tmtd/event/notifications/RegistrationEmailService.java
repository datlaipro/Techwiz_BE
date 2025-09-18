
// package tmtd.event.notifications;

// import java.util.Locale;
// import java.util.Optional;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
// import org.springframework.stereotype.Service;

// import jakarta.mail.internet.MimeMessage;
// import lombok.RequiredArgsConstructor;
// import tmtd.event.notifications.template.EmailTemplateRenderer;

// @Service
// @RequiredArgsConstructor
// @ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = false)
// @ConditionalOnBean(JavaMailSender.class) // ✅ chỉ yêu cầu JavaMailSender
// public class RegistrationEmailService implements RegistrationEmailPort {

//     private static final Logger log = LoggerFactory.getLogger(RegistrationEmailService.class);

//     private final JavaMailSender mailSender;
//     private final Optional<EmailTemplateRenderer> renderer; // ✅ optional

//     @Value("${app.mail.from:EventSphere <no-reply@localhost>}")
//     private String from;

//     @Value("${app.mail.replyTo:}")
//     private String replyTo;

//     @Override
//     public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
//         if (to == null || to.isBlank()) {
//             log.warn("Skip send mail: empty recipient for eventId={} regId={}", eventId, registrationId);
//             return;
//         }
//         try {
//             String html = renderer
//                 .map(r -> r.renderRegistrationSuccess(eventTitle, eventId, registrationId, new Locale("vi")))
//                 .orElseGet(() -> """
//                     <html><body>
//                       <h3>Đăng ký thành công</h3>
//                       <p>Sự kiện: <b>%s</b></p>
//                       <p>Mã đăng ký: <b>%d</b></p>
//                     </body></html>
//                     """.formatted(eventTitle, registrationId));

//             MimeMessage msg = mailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(
//                 msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8"
//             );
//             helper.setFrom(from);
//             helper.setTo(to);
//             helper.setSubject("[EventSphere] Đăng ký thành công: " + eventTitle);
//             if (replyTo != null && !replyTo.isBlank()) helper.setReplyTo(replyTo);
//             helper.setText(html, true);

//             mailSender.send(msg);
//             log.info("Sent registration email to {} for eventId={}, regId={}", to, eventId, registrationId);
//         } catch (Exception ex) {
//             // Không phá vỡ luồng nghiệp vụ
//             log.warn("Send mail failed to {} (eventId={}, regId={}): {}", to, eventId, registrationId, ex.toString());
//         }
//     }
// }




package tmtd.event.notifications;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import tmtd.event.notifications.template.EmailTemplateRenderer;

/**
 * Impl gửi mail thật.
 * Không gắn @Service/@Component — bean được tạo trong RegistrationEmailAutoConfig.
 */
@RequiredArgsConstructor
public class RegistrationEmailService implements RegistrationEmailPort {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEmailService.class);

    private final JavaMailSender mailSender;
    private final Optional<EmailTemplateRenderer> renderer;

    @Value("${app.mail.from:EventSphere <no-reply@localhost>}")
    private String from;

    @Value("${app.mail.replyTo:}")
    private String replyTo;

    @Override
    public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
        if (to == null || to.isBlank()) {
            log.warn("Skip send mail: empty recipient for eventId={} regId={}", eventId, registrationId);
            return;
        }

        try {
            String html = renderer
                .map(r -> r.renderRegistrationSuccess(eventTitle, eventId, registrationId, new Locale("vi")))
                .orElseGet(() -> """
                    <html><body>
                      <h3>Đăng ký thành công</h3>
                      <p>Sự kiện: <b>%s</b></p>
                      <p>Mã đăng ký: <b>%d</b></p>
                    </body></html>
                    """.formatted(eventTitle, registrationId));

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8"
            );
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("[EventSphere] Đăng ký thành công: " + eventTitle);
            if (replyTo != null && !replyTo.isBlank()) helper.setReplyTo(replyTo);
            helper.setText(html, true);

            mailSender.send(msg);
            log.info("Sent registration email to {} for eventId={}, regId={}", to, eventId, registrationId);
        } catch (Exception ex) {
            // Không làm hỏng flow nghiệp vụ
            log.warn("Send mail failed to {} (eventId={}, regId={}): {}", to, eventId, registrationId, ex.toString());
        }
    }
}
