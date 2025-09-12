package tmtd.event.events.image;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tmtd.event.events.EntityEvents;
import tmtd.event.events.JpaEvents;

@Service
@Transactional
public class ServiceImage {

    private final JpaImage jpaImage;
    private final JpaEvents jpaEvents;

    public ServiceImage(JpaImage jpaImage, JpaEvents jpaEvents) {
        this.jpaImage = jpaImage;
        this.jpaEvents = jpaEvents;
    }

    public List<EntityImage> listByEvent(Long eventId) {
        return jpaImage.findByEvent_EventId(eventId);
    }

    public EntityImage addImage(Long eventId, String fileUrl, String caption, String fileType) {
        EntityEvents ev = jpaEvents.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        EntityImage img = new EntityImage();
        img.setEvent(ev);
        img.setFileUrl(fileUrl);
        img.setCaption(caption);
        if (fileType != null && !fileType.isBlank()) {
            img.setFileType(fileType);
        }
        return jpaImage.save(img);
    }

    public void delete(Long imageId) {
        jpaImage.deleteById(imageId);
    }
}
