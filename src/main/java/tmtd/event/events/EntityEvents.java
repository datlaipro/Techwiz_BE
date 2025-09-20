
package tmtd.event.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import tmtd.event.events.image.EntityImage;

@Entity
@Table(name = "events")
public class EntityEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category; // technical / cultural / ...

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(length = 100)
    private String venue;

    // Người tổ chức (FK Users.user_id) – dùng số nguyên để tránh phụ thuộc module
    // users
    @Column(nullable = false)
    private Long organizerId;

    // Map đúng tên cột trong DB: approval_status
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT; // hoặc PENDING nếu muốn mặc định chờ duyệt

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    // Sức chứa thiết kế (có thể null nếu không giới hạn)
    @Column(name = "total_seats")
    private Long totalSeats;

    // // GHẾ CÒN TRỐNG: dùng để trừ/bù khi đăng ký/hủy
    // @Column(name = "seats_available", nullable = false)
    // private Long seatsAvailable = 0L;

    // Ảnh đại diện chính (thumbnail/banner)
    @Column(name = "main_image_url", length = 512)
    private String mainImageUrl;

    // Optimistic locking
    @Version
    @Column(nullable = false)
    private Long version;

    // Thời điểm tạo/cập nhật
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Sự kiện nhiều ngày
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Gallery ảnh (bảng media_gallery)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntityImage> images = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null)
            createdAt = OffsetDateTime.now();
        if (updatedAt == null)
            updatedAt = createdAt;
    }

    @PreUpdate
    public void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ===== Getters/Setters =====
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Long totalSeats) {
        this.totalSeats = totalSeats;
    }

    // public Long getSeatsAvailable() { return seatsAvailable; }
    // public void setSeatsAvailable(Long seatsAvailable) { this.seatsAvailable =
    // seatsAvailable; }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<EntityImage> getImages() {
        return images;
    }

    public void setImages(List<EntityImage> images) {
        this.images = images;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
