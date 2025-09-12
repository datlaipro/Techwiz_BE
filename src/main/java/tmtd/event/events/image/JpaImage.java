package tmtd.event.events.image;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaImage extends JpaRepository<EntityImage, Long> {
    List<EntityImage> findByEvent_EventId(Long eventId);
}
