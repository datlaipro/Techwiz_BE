package tmtd.event.feedback.dto;

import java.time.Instant;

public record FeedbackResponse(
        Long feedbackId,
        Long eventId,
        Integer studentId,
        int rating,
        String comments,
        Instant submittedOn
) {}
