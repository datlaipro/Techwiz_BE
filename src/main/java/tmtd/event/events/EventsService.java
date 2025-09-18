


// package tmtd.event.events;

// import java.util.List;

// import tmtd.event.events.dto.EventCreateRequest;
// import tmtd.event.events.dto.EventResponse;
// import tmtd.event.events.dto.EventStatsResponse;
// import tmtd.event.events.dto.EventUpdateRequest;

// public interface EventsService {
//     // public
//     List<EventResponse> listApproved();

//     EventResponse getApprovedById(Long eventId);

//     List<EventResponse> listApprovedByCategory(String category); // <- đã giữ lại

//     List<EventResponse> listPendingApproveEvents(); // <- thêm lại như yêu cầu

//     EventResponse getPendingEventById(Long eventId);

//     EventResponse getPendingEventByIdForOrganizer(Long organizerId, Long eventId);

//     // ✅ THÊM method create chung (ADMIN/ORGANIZER)
//     EventResponse create(EventCreateRequest req);

//     // organizer (giữ để tương thích; sẽ delegate sang create())
//     EventResponse createAsOrganizer(Long organizerId, EventCreateRequest req);

//     EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req);

//     void deleteByOwner(Long organizerId, Long eventId);

//     EventResponse submitForApproval(Long organizerId, Long eventId);

//     List<EventResponse> listByOrganizer(Long organizerId);

//     EventStatsResponse statsForOrganizer(Long organizerId, Long eventId);

//     // admin
//     EventResponse approve(Long adminId, Long eventId);

//     EventResponse reject(Long adminId, Long eventId);

//     // hệ thống
//     SystemStatsResponse systemStats();

//     record SystemStatsResponse(long totalEvents, long approved, long pending, long rejected) {
//     }
// }





package tmtd.event.events;

import java.util.List;

import tmtd.event.events.dto.EventCreateRequest;
import tmtd.event.events.dto.EventResponse;
import tmtd.event.events.dto.EventStatsResponse;
import tmtd.event.events.dto.EventUpdateRequest;

public interface EventsService {

    void deleteByEventId(Long eventId);
    
    // public
    List<EventResponse> listApproved();

    EventResponse getApprovedById(Long eventId);

    List<EventResponse> listApprovedByCategory(String category); // <- đã giữ lại

    List<EventResponse> listPendingApproveEvents(); // <- thêm lại như yêu cầu

    EventResponse getPendingEventById(Long eventId);

    EventResponse getPendingEventByIdForOrganizer(Long organizerId, Long eventId);

    // ✅ THÊM method create chung (ADMIN/ORGANIZER)
    EventResponse create(EventCreateRequest req);

    // organizer (giữ để tương thích; sẽ delegate sang create())
    EventResponse createAsOrganizer(Long organizerId, EventCreateRequest req);

    EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req);

    default void deleteByOwner(Long organizerId, Long eventId) {
        deleteByEventId(eventId);
    }

    EventResponse submitForApproval(Long organizerId, Long eventId);

    List<EventResponse> listByOrganizer(Long organizerId);

    EventStatsResponse statsForOrganizer(Long organizerId, Long eventId);

    // admin
    EventResponse approve(Long adminId, Long eventId);

    EventResponse reject(Long adminId, Long eventId);

    // hệ thống
    SystemStatsResponse systemStats();

    record SystemStatsResponse(long totalEvents, long approved, long pending, long rejected) {
    }
}
