package tmtd.event.attendance;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JpaAttendance extends JpaRepository<EntityAttendance, Long> {

  Optional<EntityAttendance> findByEventIdAndStudentId(Long eventId, Integer studentId);
  List<EntityAttendance> findByEventId(Long eventId);

  @Query("select count(distinct a.studentId) from EntityAttendance a where a.eventId = :eventId")
  long countDistinctAttendees(@Param("eventId") Long eventId);

  // native upsert-lite (insert once)
  @Transactional
  @Modifying
  @Query(value = """
      INSERT INTO attendance (event_id, student_id, attended, marked_on, method)
      SELECT :eventId, :studentId, TRUE, CURRENT_TIMESTAMP, :method
      WHERE NOT EXISTS (
        SELECT 1 FROM attendance WHERE event_id = :eventId AND student_id = :studentId
      )
      """, nativeQuery = true)
  int markAttendedOnce(@Param("eventId") Long eventId,
                       @Param("studentId") Integer studentId,
                       @Param("method") String method);
}
