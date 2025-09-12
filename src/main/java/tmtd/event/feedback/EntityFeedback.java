package tmtd.event.feedback;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feedback")
public class EntityFeedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "submitted_on", nullable = false, updatable = false)
    private Instant submittedOn = Instant.now();

    // getters/setters
    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Instant getSubmittedOn() { return submittedOn; }
    public void setSubmittedOn(Instant submittedOn) { this.submittedOn = submittedOn; }
}
