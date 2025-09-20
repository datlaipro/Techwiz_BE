package tmtd.event.registrations.dto;

import java.time.LocalDate;
import java.time.LocalTime;
// kiểu trả về số lượng
public class RegistrationEventItemWithStats {
  public Long eventId;
  public String title;
  public String category;
  public String venue;
  public LocalDate date;
  public LocalDate startDate;
  public LocalDate endDate;
  public LocalTime time;
  public Integer totalSeats;
  public String mainImageUrl;

  public Long registeredCount; // số người đã đăng ký (CONFIRMED)
  public Long checkinCount;    // số người đã check-in (REDEEMED)
  public Long seatsRemaining;  // = totalSeats - registeredCount (tính sẵn cho tiện FE)

  public RegistrationEventItemWithStats(
      Long eventId, String title, String category, String venue,
      LocalDate date, LocalDate startDate, LocalDate endDate, LocalTime time,
      Integer totalSeats, String mainImageUrl,
      Long registeredCount, Long checkinCount
  ) {
    this.eventId = eventId;
    this.title = title;
    this.category = category;
    this.venue = venue;
    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;
    this.time = time;
    this.totalSeats = totalSeats;
    this.mainImageUrl = mainImageUrl;
    this.registeredCount = registeredCount != null ? registeredCount : 0L;
    this.checkinCount = checkinCount != null ? checkinCount : 0L;
    this.seatsRemaining = totalSeats != null
        ? Math.max(0L, totalSeats.longValue() - this.registeredCount)
        : 0L;
  }
}
