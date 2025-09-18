




// package tmtd.event.events;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import tmtd.event.config.AuthFacade;
// import tmtd.event.events.dto.EventCreateRequest;
// import tmtd.event.events.dto.EventResponse;
// import tmtd.event.events.dto.EventStatsResponse;
// import tmtd.event.events.dto.EventUpdateRequest;

// @RestController
// @RequestMapping("/api/organizer/events")
// @RequiredArgsConstructor
// public class OrganizerEventsController {

//     private final EventsService events;
//     private final AuthFacade auth;

//     /** Tạo sự kiện: ADMIN hoặc ORGANIZER */
//     @PostMapping
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
//     public ResponseEntity<EventResponse> create(@RequestBody @Valid EventCreateRequest req) {
//         var res = events.create(req);
//         return ResponseEntity.status(HttpStatus.CREATED).body(res);
//     }

//     /** Cập nhật: Admin hoặc chính chủ event */
//     @PutMapping("/{eventId}")
//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     public EventResponse update(@PathVariable Long eventId, @RequestBody @Valid EventUpdateRequest req) {
//         Long me = auth.currentUserId();
//         // Vẫn gọi theo signature cũ để giữ mustOwn + thông báo lỗi tùy biến
//         return events.updateByOwner(me, eventId, req);
//     }

//     /** Xoá: Admin hoặc chính chủ event */
//     @DeleteMapping("/{eventId}")
//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     public ResponseEntity<Void> delete(@PathVariable Long eventId) {
//         Long me = auth.currentUserId();
//         events.deleteByOwner(me, eventId);
//         return ResponseEntity.noContent().build();
//     }

//     /** Nộp duyệt: Admin hoặc chính chủ event */
//     @PostMapping("/{eventId}/submit")
//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     public EventResponse submit(@PathVariable Long eventId) {
//         Long me = auth.currentUserId();
//         return events.submitForApproval(me, eventId);
//     }

//     /** Danh sách sự kiện của chính mình (Organizer/Admin) */
//     @GetMapping
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
//     public List<EventResponse> myEvents() {
//         Long me = auth.currentUserId();
//         return events.listByOrganizer(me);
//     }

//     /** Thống kê sự kiện của chính mình (Organizer) */
//     @GetMapping("/{eventId}/stats")
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER') and @auth.isOwnerOfEvent(#eventId)")
//     public EventStatsResponse stats(@PathVariable Long eventId) {
//         Long me = auth.currentUserId();
//         return events.statsForOrganizer(me, eventId);
//     }

//     @GetMapping("/{organizerId}/pending/{eventId}")
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
//     public EventResponse getPendingEventForOrganizer(@PathVariable Long organizerId, @PathVariable Long eventId) {
//         return events.getPendingEventByIdForOrganizer(organizerId, eventId);
//     }

// }




package tmtd.event.events;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tmtd.event.config.AuthFacade;
import tmtd.event.events.dto.EventCreateRequest;
import tmtd.event.events.dto.EventResponse;
import tmtd.event.events.dto.EventStatsResponse;
import tmtd.event.events.dto.EventUpdateRequest;
import tmtd.event.events.EventsService;

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventsController {

    private final EventsService events;
    private final AuthFacade auth;

    /** Tạo sự kiện: ADMIN hoặc ORGANIZER */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<EventResponse> create(@RequestBody @Valid EventCreateRequest req) {
        var res = events.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /** Cập nhật: Admin hoặc chính chủ event */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    public EventResponse update(@PathVariable Long eventId, @RequestBody @Valid EventUpdateRequest req) {
        Long me = auth.currentUserId();
        // Vẫn gọi theo signature cũ để giữ mustOwn + thông báo lỗi tùy biến
        return events.updateByOwner(me, eventId, req);
    }

    /** Xoá: Admin hoặc chính chủ event */
    /** Xoá: Admin hoặc chính chủ event */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    public ResponseEntity<Void> delete(@PathVariable Long eventId) {
        events.deleteByEventId(eventId); // ✅ chỉ cần eventId
        return ResponseEntity.noContent().build();
    }

    /** Nộp duyệt: Admin hoặc chính chủ event */
    @PostMapping("/{eventId}/submit")
    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    public EventResponse submit(@PathVariable Long eventId) {
        Long me = auth.currentUserId();
        return events.submitForApproval(me, eventId);
    }

    /** Danh sách sự kiện của chính mình (Organizer/Admin) */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<EventResponse> myEvents() {
        Long me = auth.currentUserId();
        return events.listByOrganizer(me);
    }

    /** Thống kê sự kiện của chính mình (Organizer) */
    @GetMapping("/{eventId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER') and @auth.isOwnerOfEvent(#eventId)")
    public EventStatsResponse stats(@PathVariable Long eventId) {
        Long me = auth.currentUserId();
        return events.statsForOrganizer(me, eventId);
    }

    @GetMapping("/{organizerId}/pending/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public EventResponse getPendingEventForOrganizer(@PathVariable Long organizerId, @PathVariable Long eventId) {
        return events.getPendingEventByIdForOrganizer(organizerId, eventId);
    }

}
