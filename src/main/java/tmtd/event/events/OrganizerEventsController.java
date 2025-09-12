// package tmtd.event.events;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;
// import tmtd.event.events.dto.*;
// import tmtd.event.config.AuthFacade;

// import java.util.List;

// @RestController
// @RequestMapping("/api/organizer/events")
// @RequiredArgsConstructor
// public class OrganizerEventsController {

//     private final EventsService service;
//     private final AuthFacade auth;

//     @PostMapping
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public EventResponse create(@Valid @RequestBody EventCreateRequest req){
//         return service.createAsOrganizer(auth.currentUserId(), req);
//     }

//     @PutMapping("/{id}")
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest req){
//         return service.updateByOwner(auth.currentUserId(), id, req);
//     }

//     @DeleteMapping("/{id}")
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public void delete(@PathVariable Long id){
//         service.deleteByOwner(auth.currentUserId(), id);
//     }

//     @PostMapping("/{id}/submit")
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public EventResponse submit(@PathVariable Long id){
//         return service.submitForApproval(auth.currentUserId(), id);
//     }

//     @GetMapping("/mine")
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public List<EventResponse> mine(){
//         return service.listByOrganizer(auth.currentUserId());
//     }

//     @GetMapping("/{id}/stats")
//     @PreAuthorize("hasRole('ORGANIZER')")
//     public EventStatsResponse stats(@PathVariable Long id){
//         return service.statsForOrganizer(auth.currentUserId(), id);
//     }
// }



package tmtd.event.events;

import java.util.List;

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

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventsController {

    private final EventsService service;
    private final AuthFacade auth;

    // @PostMapping
    // @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    // public EventResponse create(@Valid @RequestBody EventCreateRequest req) {
    //     return service.createAsOrganizer(auth.currentUserId(), req);
    // }

        @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public EventResponse create(@Valid @RequestBody EventCreateRequest req) {
        // KHÔNG truyền organizerId từ currentUser nữa, để service quyết theo role
        return service.create(req);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest req) {
        return service.updateByOwner(auth.currentUserId(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public void delete(@PathVariable Long id) {
        service.deleteByOwner(auth.currentUserId(), id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse submit(@PathVariable Long id) {
        return service.submitForApproval(auth.currentUserId(), id);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<EventResponse> mine() {
        return service.listByOrganizer(auth.currentUserId());
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventStatsResponse stats(@PathVariable Long id) {
        return service.statsForOrganizer(auth.currentUserId(), id);
    }
}
