// src/main/java/tmtd/event/registrations/dto/EventCountersDto.java
package tmtd.event.registrations.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventCountersDto(
    Long eventId,
    String title,
    String category,
    String venue,
    LocalDate date,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime time,
    Long capacity,
    Long totalSeat,        // ✅ THÊM
    Long registeredCount,
    Long checkinCount,
    Long seatsRemaining
) {}
