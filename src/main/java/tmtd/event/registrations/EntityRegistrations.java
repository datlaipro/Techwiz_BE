package tmtd.event.registrations;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
    name = "registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","student_id"})
)
public class EntityRegistrations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;      // FK -> events.event_id (EntityEvents.eventId)

    @Column(name = "student_id", nullable = false)
    private Long studentId; // FK -> users.user_id (EntityUser.user_id)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    @Column(name = "registered_on", nullable = false, updatable = false)
    private Instant registeredOn;

    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @Version
    private Long version;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.registeredOn = now;
        this.updatedOn = now;
        if (this.status == null) this.status = RegistrationStatus.CONFIRMED;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedOn = Instant.now();
    }

    // Getters/Setters
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public Instant getRegisteredOn() { return registeredOn; }
    public Instant getUpdatedOn() { return updatedOn; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
