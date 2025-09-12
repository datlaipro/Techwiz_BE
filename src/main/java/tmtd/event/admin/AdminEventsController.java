// package tmtd.event.admin;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import tmtd.event.events.EventsService;
// import tmtd.event.events.dto.EventCreateRequest;
// import tmtd.event.events.dto.EventResponse;

// @RestController
// @RequestMapping("/api/admin")
// @RequiredArgsConstructor
// public class AdminEventsController {

//     private final EventsService events;

//     // POST /api/admin/organizers/{organizerId}/events
//     @PostMapping("/organizers/{organizerId}/events")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<EventResponse> createForOrganizer(
//             @PathVariable Long organizerId,
//             @RequestBody @Valid EventCreateRequest req
//     ) {
//         var res = events.createAsOrganizer(organizerId, req);
//         return ResponseEntity.status(HttpStatus.CREATED).body(res);
//     }
// }

package tmtd.event.admin;

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
import tmtd.event.events.EventsService;
import tmtd.event.events.dto.EventCreateRequest;
import tmtd.event.events.dto.EventResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminEventsController {

    private final EventsService events;

    @GetMapping("/events/pending-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getPendingApproveEvents() {
        List<EventResponse> eventsList = events.listPendingApproveEvents();
        return ResponseEntity.ok(eventsList);
    }

    // POST /api/admin/organizers/{organizerId}/events
    @PostMapping("/organizers/{organizerId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createForOrganizer(
            @PathVariable Long organizerId,
            @RequestBody @Valid EventCreateRequest req) {
        var res = events.createAsOrganizer(organizerId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}


