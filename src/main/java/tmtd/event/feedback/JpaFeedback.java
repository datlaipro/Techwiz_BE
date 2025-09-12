package tmtd.event.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaFeedback extends JpaRepository<EntityFeedback, Long> {
    List<EntityFeedback> findByEventId(Long eventId);
    List<EntityFeedback> findByStudentId(Integer studentId);
}
