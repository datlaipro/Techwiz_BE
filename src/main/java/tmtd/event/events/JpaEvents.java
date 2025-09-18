

// package tmtd.event.events;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.transaction.annotation.Transactional;

// public interface JpaEvents extends JpaRepository<EntityEvents, Long>,
//                                    JpaSpecificationExecutor<EntityEvents> {

//     List<EntityEvents> findByCategoryIgnoreCase(String category);

//     List<EntityEvents> findByDateBetween(LocalDate from, LocalDate to);

//     // 🔧 Lấy sự kiện theo trạng thái
//     List<EntityEvents> findByStatus(EventStatus status);

//     // 🔧 Lấy danh sách sự kiện theo organizer
//     List<EntityEvents> findByOrganizerId(Long organizerId);

//     List<EntityEvents> findByCategoryIgnoreCaseAndStatus(String category, EventStatus status);

//     // 🔧 Kiểm tra quyền sở hữu sự kiện
//     boolean existsByEventIdAndOrganizerId(Long eventId, Long organizerId);

//     // 🔧 Method mới: tìm sự kiện theo ID và organizer ID
//     Optional<EntityEvents> findByEventIdAndOrganizerId(Long eventId, Long organizerId);

//     // 🔥 Cập nhật ghế còn trống khi đăng ký
//     @Modifying(clearAutomatically = true, flushAutomatically = true)
//     @Transactional
//     @Query("""
//         UPDATE EntityEvents e
//            SET e.seatsAvailable = e.seatsAvailable - 1
//          WHERE e.eventId = :id
//            AND e.seatsAvailable > 0
//     """)
//     int tryConsumeSeat(@Param("id") Long eventId);

//     // 🔥 Hoàn trả ghế khi hủy đăng ký
//     @Modifying(clearAutomatically = true, flushAutomatically = true)
//     @Transactional
//     @Query("""
//         UPDATE EntityEvents e
//            SET e.seatsAvailable = e.seatsAvailable + 1
//          WHERE e.eventId = :id
//     """)
//     int releaseSeat(@Param("id") Long eventId);
// }




package tmtd.event.events;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

  // 🔧 Lấy sự kiện theo trạng thái
  List<EntityEvents> findByStatus(EventStatus status);

  // 🔧 Lấy danh sách sự kiện theo organizer
  List<EntityEvents> findByOrganizerId(Long organizerId);

  List<EntityEvents> findByCategoryIgnoreCaseAndStatus(String category, EventStatus status);

  // 🔧 Kiểm tra quyền sở hữu sự kiện
  boolean existsByEventIdAndOrganizerId(Long eventId, Long organizerId);

  // 🔧 Method mới: tìm sự kiện theo ID và organizer ID
  Optional<EntityEvents> findByEventIdAndOrganizerId(Long eventId, Long organizerId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from EntityEvents e where e.eventId = :id")
  int hardDeleteById(Long id);

  // 🔥 Cập nhật ghế còn trống khi đăng ký
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
          UPDATE EntityEvents e
             SET e.seatsAvailable = e.seatsAvailable - 1
           WHERE e.eventId = :id
             AND e.seatsAvailable > 0
      """)
  int tryConsumeSeat(@Param("id") Long eventId);

  // 🔥 Hoàn trả ghế khi hủy đăng ký
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
          UPDATE EntityEvents e
             SET e.seatsAvailable = e.seatsAvailable + 1
           WHERE e.eventId = :id
      """)
  int releaseSeat(@Param("id") Long eventId);
}
