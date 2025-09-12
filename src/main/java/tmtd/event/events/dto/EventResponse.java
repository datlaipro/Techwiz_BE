package tmtd.event.events.dto;

import tmtd.event.events.EventStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(
    Long eventId,
    String title,
    String description,
    String category,
    LocalDate date,
    LocalTime time,
        // ⬇️ Thêm cho sự kiện nhiều ngày (tùy chọn)
    LocalDate startDate,
    LocalDate endDate,
    String venue,
    Long organizerId,
    EventStatus status,
    Long approvedBy,
    Instant approvedAt,
    Integer totalSeats,
    String mainImageUrl // <-- thêm
) {}
