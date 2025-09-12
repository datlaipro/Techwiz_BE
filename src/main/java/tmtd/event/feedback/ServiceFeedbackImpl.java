package tmtd.event.feedback;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmtd.event.feedback.dto.FeedbackCreateRequest;
import tmtd.event.feedback.dto.FeedbackResponse;
import tmtd.event.feedback.dto.FeedbackUpdateRequest;

import java.time.Instant;
import java.util.List;

@Service
public class ServiceFeedbackImpl implements ServiceFeedback {

    private final JpaFeedback repo;

    public ServiceFeedbackImpl(JpaFeedback repo) {
        this.repo = repo;
    }

    private FeedbackResponse toDto(EntityFeedback f) {
        return new FeedbackResponse(
                f.getFeedbackId(),
                f.getEventId(),
                f.getStudentId(),
                f.getRating(),
                f.getComments(),
                f.getSubmittedOn()
        );
    }

    @Override
    @Transactional
    public FeedbackResponse create(FeedbackCreateRequest req) {
        var f = new EntityFeedback();
        f.setEventId(req.eventId());
        f.setStudentId(req.studentId());
        f.setRating(req.rating());
        f.setComments(req.comments());
        f.setSubmittedOn(Instant.now());
        return toDto(repo.save(f));
    }

    @Override
    @Transactional
    public FeedbackResponse update(Long id, FeedbackUpdateRequest req) {
        var f = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
        if (req.rating() != null) f.setRating(req.rating());
        if (req.comments() != null) f.setComments(req.comments());
        return toDto(repo.save(f));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Feedback not found: " + id);
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse get(Long id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> listByEvent(Long eventId) {
        return repo.findByEventId(eventId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> listByStudent(Integer studentId) {
        return repo.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double avgRating(Long eventId) {
        // Nếu muốn có method này, thêm @Query avg ở JpaFeedback; không bắt buộc để chạy CRUD.
        return repo.findByEventId(eventId).stream().mapToInt(EntityFeedback::getRating).average().orElse(Double.NaN);
    }
}
