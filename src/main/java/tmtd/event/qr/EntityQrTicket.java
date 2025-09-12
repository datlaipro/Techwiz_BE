package tmtd.event.qr;

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
    name = "qr_tickets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "student_id"}),
        @UniqueConstraint(columnNames = {"token"})
    }
)
public class EntityQrTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;   // FK -> events.event_id (EntityEvents.eventId)

    @Column(name = "student_id", nullable = false)
    private Integer studentId; // FK -> users.user_id (EntityUser.user_id)

    @Column(nullable = false, unique = true, length = 64)
    private String token;   // Mã token QR duy nhất toàn hệ thống

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "issued_on", nullable = false, updatable = false)
    private Instant issuedOn;

    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @Column(name = "expires_at")
    private Instant expiresAt;  // Thời hạn vé, null = không hết hạn

    @Column(name = "used_at")
    private Instant usedAt;     // Thời điểm vé được quét

    @Column(name = "used_by")
    private Long usedBy;        // ID người quét vé (admin/organizer)

    @Version
    private Long version;

    // ===== Enum trạng thái vé =====
    public enum TicketStatus {
        ACTIVE,    // Vé mới phát, chưa quét
        REDEEMED,  // Đã quét và check-in
        REVOKED    // Hủy vé
    }

    // ===== Lifecycle hooks =====
    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.issuedOn = now;
        this.updatedOn = now;
        if (this.status == null) this.status = TicketStatus.ACTIVE;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedOn = Instant.now();
    }

    // ===== Getters/Setters =====
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public Instant getIssuedOn() { return issuedOn; }
    public Instant getUpdatedOn() { return updatedOn; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }

    public Long getUsedBy() { return usedBy; }
    public void setUsedBy(Long usedBy) { this.usedBy = usedBy; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
