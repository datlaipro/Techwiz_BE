package tmtd.event.registrations.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

public class RegistrationCreateRequest {

    @NotNull
    private Long eventId;

    // server tự gắn từ token -> KHÔNG validate NotNull ở đây, và ẩn khỏi client
    @JsonIgnore
    private Long studentId;

    // getters & setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
