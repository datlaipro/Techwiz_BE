package tmtd.event.notifications;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
public class TestMailController {

    private final RegistrationEmailPort email; // sẽ là impl thật nếu mail.enabled=true + SMTP ok, nếu không sẽ là NO-OP

    @GetMapping("/mail/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "msg", "mail test endpoint is alive");
    }

    @PostMapping("/mail")
    public Map<String, Object> send(@RequestBody TestMailRequest req) {
        email.sendRegistrationSuccess(req.to(), req.eventTitle(), req.eventId(), req.registrationId());
        return Map.of(
            "ok", true,
            "to", req.to(),
            "eventTitle", req.eventTitle(),
            "eventId", req.eventId(),
            "registrationId", req.registrationId()
        );
    }

    public record TestMailRequest(
        String to,
        String eventTitle,
        Long eventId,
        Long registrationId
    ) {}
}
