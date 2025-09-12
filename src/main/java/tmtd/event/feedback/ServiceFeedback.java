package tmtd.event.feedback;

import tmtd.event.feedback.dto.*;
import java.util.List;

public interface ServiceFeedback {
    FeedbackResponse create(FeedbackCreateRequest req);
    FeedbackResponse update(Long id, FeedbackUpdateRequest req);
    void delete(Long id);

    FeedbackResponse get(Long id);
    List<FeedbackResponse> listByEvent(Long eventId);
    List<FeedbackResponse> listByStudent(Integer studentId);

    Double avgRating(Long eventId);
}
