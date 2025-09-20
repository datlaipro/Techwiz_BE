
package tmtd.event.registrations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRegistrations extends JpaRepository<EntityRegistrations, Long> {

  // Kiểm tra học viên đã đăng ký sự kiện chưa
  boolean existsByEventIdAndStudentId(long eventId, long studentId);

  // Tìm bản đăng ký theo eventId và studentId (trả Optional)
  Optional<EntityRegistrations> findByEventIdAndStudentId(Long eventId, Long studentId);

  Long countByEventId(Long eventId);

  // Đếm số bản đăng ký theo trạng thái
  Long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

  // Lấy tất cả bản đăng ký của một sự kiện
  List<EntityRegistrations> findAllByEventId(Long eventId);

  // Lấy tất cả bản đăng ký của một học viên
  List<EntityRegistrations> findAllByStudentId(Long studentId);

  // [ADD] Tìm bản đăng ký với studentId kiểu Long (để đồng nhất với nơi khác
  // trong code)
  Optional<EntityRegistrations> findFirstByEventIdAndStudentId(Long eventId, Long studentId);

  // [ADD] Kiểm tra bản đăng ký có đúng trạng thái (với studentId kiểu Long)
  boolean existsByEventIdAndStudentIdAndStatus(Long eventId, Long studentId, RegistrationStatus status);

  @org.springframework.data.jpa.repository.Query("""
      select new tmtd.event.registrations.dto.RegistrationEventItem(
          e.eventId, e.title, e.category, e.venue,
          e.date, e.startDate, e.endDate, e.time,
          e.totalSeats, e.mainImageUrl

      )
      from EntityRegistrations r
      join tmtd.event.events.EntityEvents e on e.eventId = r.eventId
      where r.studentId = :studentId
        and (:status is null or r.status = :status)
        and (
             :whenMode = 'ALL'
          or (:whenMode = 'UPCOMING' and e.date >= :today)
          or (:whenMode = 'PAST'     and e.date  < :today)
        )
      """)
  org.springframework.data.domain.Page<tmtd.event.registrations.dto.RegistrationEventItem> findUserRegisteredEvents(
      @org.springframework.data.repository.query.Param("studentId") Long studentId,
      @org.springframework.data.repository.query.Param("status") tmtd.event.registrations.RegistrationStatus status,
      @org.springframework.data.repository.query.Param("whenMode") String whenMode, // ALL | UPCOMING | PAST
      @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
      org.springframework.data.domain.Pageable pageable);

}
