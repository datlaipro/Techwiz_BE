package tmtd.event.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
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

    // Người tổ chức (FK Users.user_id) – dùng số nguyên để tránh phụ thuộc module users
    @Column(nullable = false)
    private Long organizerId;

    // Map đúng tên cột trong DB: approval_status
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT; // hoặc PENDING nếu bạn muốn mặc định đang chờ duyệt

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    // Sức chứa (có thể null nếu không giới hạn)
    private Integer totalSeats;

    // Ảnh đại diện chính (thumbnail/banner) — để FE gửi lên dưới dạng URL
    @Column(name = "main_image_url", length = 512)
    private String mainImageUrl;

    // Thời điểm tạo/cập nhật
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

        // ⬇️ Thêm 2 trường NGÀY BẮT ĐẦU / KẾT THÚC
    @Column(name = "start_date")
    private LocalDate startDate;   // sự kiện nhiều ngày: ngày bắt đầu

    @Column(name = "end_date")
    private LocalDate endDate;     // sự kiện nhiều ngày: ngày kết thúc

    // Gallery ảnh (bảng media_gallery)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntityImage> images = new ArrayList<>();

    @PreUpdate
    public void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ===== Getters/Setters =====
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public List<EntityImage> getImages() { return images; }
    public void setImages(List<EntityImage> images) { this.images = images; }

        // ⬇️ Thêm getters/setters cho 2 trường mới
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
