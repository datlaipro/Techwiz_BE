package tmtd.event.events.image;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tmtd.event.events.EntityEvents;

@Entity
@Table(name = "media_gallery")
public class EntityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EntityEvents event;

    @Column(nullable = false, length = 10)
    private String fileType = "image"; // image|video

    @Column(nullable = false, length = 255)
    private String fileUrl;

    @Column(length = 150)
    private String caption;

    @Column(nullable = false)
    private OffsetDateTime uploadedOn = OffsetDateTime.now();

    // ===== Getters/Setters =====
    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }
    public EntityEvents getEvent() { return event; }
    public void setEvent(EntityEvents event) { this.event = event; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public OffsetDateTime getUploadedOn() { return uploadedOn; }
    public void setUploadedOn(OffsetDateTime uploadedOn) { this.uploadedOn = uploadedOn; }
}
