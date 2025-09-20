package tmtd.event.registrations.dto;
// trả về thông tin sự kiện mà user đăng kí 
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class RegistrationEventItem {
    // Event
   private Long eventId;
    private String title;
    private String category;
    private String venue;
    private LocalDate date;
    private LocalDate startDate; 
    private LocalDate endDate;
    private LocalTime time;
    private Long totalSeats;
    private String mainImageUrl;

    // Registration
   

    public RegistrationEventItem(
            Long eventId, String title, String category, String venue,
            LocalDate date, LocalDate startDate, LocalDate endDate, LocalTime time,
            Long totalSeats,  String mainImageUrl
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
        
    }

    // getters (cần cho Jackson)
    public Long getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getVenue() { return venue; }
    public LocalDate getDate() { return date; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalTime getTime() { return time; }
    public Long getTotalSeats() { return totalSeats; }
    public String getMainImageUrl() { return mainImageUrl; }

}
