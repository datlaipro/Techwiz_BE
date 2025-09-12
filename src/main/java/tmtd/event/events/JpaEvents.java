// package tmtd.event.events;

// import java.time.LocalDate;
// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// public interface JpaEvents extends JpaRepository<EntityEvents, Long>, JpaSpecificationExecutor<EntityEvents> {

//     List<EntityEvents> findByCategoryIgnoreCase(String category);

//     List<EntityEvents> findByDateBetween(LocalDate from, LocalDate to);

//     List<EntityEvents> findByStatus(EventStatus status);

//     List<EntityEvents> findByOrganizerId(Long organizerId);

//     @Modifying(clearAutomatically = true, flushAutomatically = true)
//     @Query("""
//               update EntityEvents e
//                  set e.totalSeats = e.totalSeats - 1
//                where e.eventId = :id
//                  and e.totalSeats > 0
//             """)
//     int tryConsumeSeat(@Param("id") Long eventId);

//     // (tùy chọn) Hoàn ghế nếu hủy đăng ký:
//     @Modifying(clearAutomatically = true, flushAutomatically = true)
//     @Query("""
//               update EntityEvents e
//                  set e.totalSeats = e.totalSeats + 1
//                where e.eventId = :id
//             """)
//     int releaseSeat(@Param("id") Long eventId);
// }


package tmtd.event.events;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JpaEvents extends JpaRepository<EntityEvents, Long>,
                                   JpaSpecificationExecutor<EntityEvents> {

    List<EntityEvents> findByCategoryIgnoreCase(String category);

    List<EntityEvents> findByDateBetween(LocalDate from, LocalDate to);

    // 🔧 Dẫn xuất đúng tên field trong EntityEvents:
    List<EntityEvents> findByStatus(EventStatus status);

    List<EntityEvents> findByOrganizerId(Long organizerId);

    boolean existsByEventIdAndOrganizerId(Long eventId, Long organizerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
          update EntityEvents e
             set e.totalSeats = e.totalSeats - 1
           where e.eventId = :id
             and e.totalSeats > 0
        """)
    int tryConsumeSeat(@Param("id") Long eventId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
          update EntityEvents e
             set e.totalSeats = e.totalSeats + 1
           where e.eventId = :id
        """)
    int releaseSeat(@Param("id") Long eventId);
}
