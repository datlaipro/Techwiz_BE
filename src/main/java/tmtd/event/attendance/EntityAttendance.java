package tmtd.event.attendance;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(
  name = "attendance",
  uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","student_id"})
)
public class EntityAttendance {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Long attendanceId;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "student_id", nullable = false)
  private Integer studentId;

  @Column(name = "attended", nullable = false)
  private boolean attended = true;

  @Column(name = "marked_on", nullable = false, updatable = false)
  private Instant markedOn = Instant.now();

  @Column(name = "method", length = 20) // "QR" | "MANUAL"
  private String method = "QR";

  public Long getAttendanceId() { return attendanceId; }
  public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
  public Long getEventId() { return eventId; }
  public void setEventId(Long eventId) { this.eventId = eventId; }
  public Integer getStudentId() { return studentId; }
  public void setStudentId(Integer studentId) { this.studentId = studentId; }
  public boolean isAttended() { return attended; }
  public void setAttended(boolean attended) { this.attended = attended; }
  public Instant getMarkedOn() { return markedOn; }
  public void setMarkedOn(Instant markedOn) { this.markedOn = markedOn; }
  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }
}
