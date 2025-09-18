


// package tmtd.event.admin;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import tmtd.event.config.AuthFacade;
// import tmtd.event.events.EventsService;
// import tmtd.event.events.dto.EventCreateRequest;
// import tmtd.event.events.dto.EventResponse;

// @RestController
// @RequestMapping("/api/admin")
// @RequiredArgsConstructor
// public class AdminEventsController {

//     private final EventsService events;
//     private final AuthFacade auth; // 👈 Thêm AuthFacade

//     /** Lấy danh sách sự kiện đang chờ duyệt */
//     @GetMapping("/events/pending-approve")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<List<EventResponse>> getPendingApproveEvents() {
//         List<EventResponse> eventsList = events.listPendingApproveEvents();
//         return ResponseEntity.ok(eventsList);
//     }

//     /** Admin tạo sự kiện thay cho organizerId */
//     @PostMapping("/organizers/{organizerId}/events")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<EventResponse> createForOrganizer(
//             @PathVariable Long organizerId,
//             @RequestBody @Valid EventCreateRequest req) {
//         var res = events.createAsOrganizer(organizerId, req);
//         return ResponseEntity.status(HttpStatus.CREATED).body(res);
//     }

// }




package tmtd.event.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tmtd.event.config.AuthFacade;
import tmtd.event.events.EventsService;
import tmtd.event.events.dto.EventCreateRequest;
import tmtd.event.events.dto.EventResponse;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminEventsController {

    private final EventsService events;
    private final AuthFacade auth; // 👈 Thêm AuthFacade

    /** Lấy danh sách sự kiện đang chờ duyệt */
    @GetMapping("/events/pending-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getPendingApproveEvents() {
        List<EventResponse> eventsList = events.listPendingApproveEvents();
        return ResponseEntity.ok(eventsList);
    }

    /** Admin tạo sự kiện thay cho organizerId */
    @PostMapping("/organizers/{organizerId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createForOrganizer(
            @PathVariable Long organizerId,
            @RequestBody @Valid EventCreateRequest req) {
        var res = events.createAsOrganizer(organizerId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/pending/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse getPendingEvent(@PathVariable Long eventId) {
        return events.getPendingEventById(eventId);
    }

}
