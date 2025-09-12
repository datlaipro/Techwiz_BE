package tmtd.event.notifications;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(JavaMailSender.class) // chỉ tạo khi có JavaMailSender
public class RegistrationEmailService implements RegistrationEmailPort {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendRegistrationSuccess(String to, String eventTitle, Long eventId, Long registrationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8"
            );
            helper.setTo(to);
            helper.setSubject("[EventSphere] Đăng ký thành công: " + eventTitle);
            helper.setFrom("no-reply@eventsphere.local");

            Context ctx = new Context();
            ctx.setVariable("eventTitle", eventTitle);
            ctx.setVariable("eventId", eventId);
            ctx.setVariable("registrationId", registrationId);

            String html = templateEngine.process("email/registration-success.html", ctx);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Gửi email thất bại", ex);
        }
    }
}
