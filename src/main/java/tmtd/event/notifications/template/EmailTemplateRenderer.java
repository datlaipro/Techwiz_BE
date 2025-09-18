package tmtd.event.notifications.template;

import java.util.Locale;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class EmailTemplateRenderer {

    private final SpringTemplateEngine templateEngine;

    public EmailTemplateRenderer(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Render Thymeleaf template to HTML.
     * @param templateName tên view không đuôi .html, ví dụ: "email/registration_success"
     * @param variables    map biến đưa vào template (có thể null)
     * @param locale       ngôn ngữ (vi, en…), null = Locale.getDefault()
     */
    public String render(@NonNull String templateName,
                         @Nullable Map<String, Object> variables,
                         @Nullable Locale locale) {
        Context ctx = new Context(locale != null ? locale : Locale.getDefault());

        // Biến mặc định chung cho mọi email (đặt thêm brand/appName, footer… nếu bạn muốn)
        ctx.setVariable("brandName", "EventSphere");

        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }

        return templateEngine.process(templateName, ctx);
    }

    // ===== Conveniences cho các email phổ biến =====

    public String renderRegistrationSuccess(String eventTitle,
                                            Long eventId,
                                            Long registrationId,
                                            @Nullable Locale locale) {
        return render(
            "email/registration_success",
            Map.of(
                "eventTitle", eventTitle,
                "eventId", eventId,
                "registrationId", registrationId
            ),
            locale
        );
    }
}
