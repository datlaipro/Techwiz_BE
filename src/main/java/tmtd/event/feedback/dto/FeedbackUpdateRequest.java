package tmtd.event.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FeedbackUpdateRequest(
        @Min(1) @Max(5) Integer rating,   // cho phép partial update
        @Size(max = 3000) String comments
) {}
