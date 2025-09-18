// package tmtd.event.events.dto;

// import tmtd.event.events.EventStatus;

// import java.time.Instant;
// import java.time.LocalDate;
// import java.time.LocalTime;

// public record EventResponse(
//     Long eventId,
//     String title,
//     String description,
//     String category,
//     LocalDate date,
//     LocalTime time,
//         // ⬇️ Thêm cho sự kiện nhiều ngày (tùy chọn)
//     LocalDate startDate,
//     LocalDate endDate,
//     String venue,
//     Long organizerId,
//     EventStatus status,
//     Long approvedBy,
//     Instant approvedAt,
//     Integer totalSeats,
//     String mainImageUrl // <-- thêm
// ) {}


// src/main/java/tmtd/event/events/dto/EventResponse.java
// src/main/java/tmtd/event/events/dto/EventResponse.java
// src/main/java/tmtd/event/events/dto/EventResponse.java
package tmtd.event.events.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import tmtd.event.events.EntityEvents;
import tmtd.event.events.EventStatus;

public record EventResponse(
        Long eventId,
        String title,
        String description,
        String category,
        LocalDate date,
        LocalTime time,
        LocalDate startDate,
        LocalDate endDate,
        String venue,
        Long organizerId,
        EventStatus status,
        Long approvedBy,
        Instant approvedAt,
        Integer totalSeats,   // DTO giữ Integer
        String mainImageUrl,
        Integer version       // DTO giữ Integer
) {
    public static EventResponse from(EntityEvents e) {
        return new EventResponse(
                e.getEventId(),
                e.getTitle(),
                e.getDescription(),
                e.getCategory(),
                e.getDate(),
                e.getTime(),
                e.getStartDate(),
                e.getEndDate(),
                e.getVenue(),
                e.getOrganizerId(),
                e.getStatus(),
                e.getApprovedBy(),
                e.getApprovedAt(),
                // 🔽 e.getTotalSeats() có thể là Long -> ép về Integer an toàn
                e.getTotalSeats() == null ? null : Math.toIntExact(e.getTotalSeats()),
                e.getMainImageUrl(),
                // 🔽 e.getVersion() có thể là Long -> ép về Integer an toàn
                e.getVersion() == null ? null : Math.toIntExact(e.getVersion())
        );
    }
}
