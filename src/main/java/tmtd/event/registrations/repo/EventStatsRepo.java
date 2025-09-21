// src/main/java/tmtd/event/registrations/EventStatsRepo.java
package tmtd.event.registrations.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import tmtd.event.events.EntityEvents;
import tmtd.event.registrations.dto.EventCountersDto;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface EventStatsRepo extends Repository<EntityEvents, Long> {

  /* ================= RAW QUERIES ================= */

 // EventStatsRepo.java

// ... giữ nguyên import/khai báo interface ...

 @Query(value = """
    SELECT
      v.event_id         AS eventId,
      v.title,
      v.category,
      v.venue,
      v.date,
      v.start_date       AS startDate,
      v.end_date         AS endDate,
      v.time,
      v.capacity,
      e.total_seats      AS totalSeat,      -- ✅ THÊM DÒNG NÀY
      v.registered_count AS registeredCount,
      v.checkin_count    AS checkinCount,
      v.seats_remaining  AS seatsRemaining
    FROM v_event_counters v
    JOIN events e ON e.event_id = v.event_id
    WHERE v.event_id = :eventId
      AND e.approval_status = 'APPROVED'
    """, nativeQuery = true)
List<Object[]> getStatsByEventId_raw(@Param("eventId") Long eventId);

@Query(value = """
    SELECT
      v.event_id         AS eventId,
      v.title,
      v.category,
      v.venue,
      v.date,
      v.start_date       AS startDate,
      v.end_date         AS endDate,
      v.time,
      v.capacity,
      e.total_seats      AS totalSeat,      -- ✅ THÊM DÒNG NÀY
      v.registered_count AS registeredCount,
      v.checkin_count    AS checkinCount,
      v.seats_remaining  AS seatsRemaining
    FROM v_event_counters v
    JOIN events e ON e.event_id = v.event_id
    WHERE e.approval_status = 'APPROVED'
    ORDER BY v.date DESC, v.time ASC
    """, nativeQuery = true)
List<Object[]> listApprovedWithStats_raw();


  // ===== API tiện dụng =====
  default java.util.Optional<EventCountersDto> getStatsByEventId(Long eventId) {
    // lấy dòng đầu tiên nếu có
    Object[] a = getStatsByEventId_raw(eventId).stream().findFirst().orElse(null);
    return (a == null) ? java.util.Optional.empty() : java.util.Optional.of(mapRowToDto(a));
  }

  default java.util.List<EventCountersDto> listApprovedWithStats() {
    // ĐỔI: method reference -> lambda để tránh lỗi “descriptor’s return type: R”
    return listApprovedWithStats_raw()
        .stream()
        .map(a -> mapRowToDto(a))
        .toList();
  }

  // ===== Helper: để static/private static đều được; để static cho gọn
  static EventCountersDto mapRowToDto(Object[] a) {
  Long   eventId = a[0] == null ? null : ((Number) a[0]).longValue();
  String title   = (String) a[1];
  String category= (String) a[2];
  String venue   = (String) a[3];

  java.time.LocalDate date      = (a[4] instanceof java.sql.Date d) ? d.toLocalDate() : null;
  java.time.LocalDate startDate = (a[5] instanceof java.sql.Timestamp ts) ? ts.toLocalDateTime().toLocalDate() : null;
  java.time.LocalDate endDate   = (a[6] instanceof java.sql.Timestamp te) ? te.toLocalDateTime().toLocalDate() : null;

  java.time.LocalTime time = null;
  if (a[7] instanceof java.sql.Time t) {
    time = t.toLocalTime();
  } else if (a[7] instanceof String s && !s.isEmpty()) {
    time = (s.length() == 5) ? java.time.LocalTime.parse(s + ":00") : java.time.LocalTime.parse(s);
  }

  Long capacity   = a[8]  == null ? null : ((Number) a[8]).longValue();
  Long totalSeat  = a[9]  == null ? null : ((Number) a[9]).longValue();      // ✅ THÊM
  Long    registered = a[10] == null ? 0L   : ((Number) a[10]).longValue();
  Long    checkin    = a[11] == null ? 0L   : ((Number) a[11]).longValue();
  Long    remaining  = a[12] == null ? 0L   : ((Number) a[12]).longValue();

  // ⚠️ CẦN constructor phù hợp trong EventCountersDto (xem mục 3)
  return new EventCountersDto(
      eventId, title, category, venue,
      date, startDate, endDate,
      time, capacity, totalSeat,        // ✅ THÊM totalSeat ngay sau capacity
      registered, checkin, remaining
  );
}


}
