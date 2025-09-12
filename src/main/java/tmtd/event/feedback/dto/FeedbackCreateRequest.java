package tmtd.event.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FeedbackCreateRequest(
        @NotNull Long eventId,
        @NotNull Integer studentId,
        @Min(1) @Max(5) int rating,
        @Size(max = 3000) String comments
) {}
