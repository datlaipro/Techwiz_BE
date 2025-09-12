package tmtd.event.feedback;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmtd.event.feedback.dto.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class ControllerFeedbackApi {

    private final ServiceFeedback service;

    public ControllerFeedbackApi(ServiceFeedback service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackCreateRequest req) {
        var created = service.create(req);
        return ResponseEntity.created(URI.create("/api/feedback/" + created.feedbackId()))
                             .body(created);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody FeedbackUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    // LIST FILTER: /api/feedback?eventId=...&studentId=...
    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> list(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Integer studentId
    ) {
        if (eventId != null) return ResponseEntity.ok(service.listByEvent(eventId));
        if (studentId != null) return ResponseEntity.ok(service.listByStudent(studentId));
        // nếu không filter, có thể trả 400 hoặc toàn bộ (tránh quá tải). Ở đây trả 400 cho rõ ràng:
        return ResponseEntity.badRequest().build();
    }

    // AVG RATING cho 1 event
    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<Double> avg(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.avgRating(eventId)); // có thể null nếu chưa có dữ liệu
    }
}
