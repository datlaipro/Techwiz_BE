// package tmtd.event.registrations;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// import tmtd.event.config.AuthFacade;
// import tmtd.event.events.EventStatus;
// import tmtd.event.events.JpaEvents;

// import tmtd.event.registrations.dto.RegistrationCreateRequest;
// import tmtd.event.registrations.dto.RegistrationResponse;

// @RestController
// @RequestMapping("/api/registrations")
// @RequiredArgsConstructor
// public class ControllerRegistrationsApi {

//     private final JpaEvents events;
//     private final ServiceRegistrations service;
//     private final AuthFacade auth;

//     @PostMapping
//     @PreAuthorize("hasRole('USER')")
//     public RegistrationResponse register(@Valid @RequestBody RegistrationCreateRequest req) {
//         // 1) kiểm tra event
//         var e = events.findById(req.getEventId())
//                       .orElseThrow(() -> new IllegalArgumentException("Event not found"));
//         if (e.getStatus() != EventStatus.APPROVED) {
//             throw new IllegalStateException("Event not open for registration");
//         }

//         // 2) gắn studentId từ token (Repo của bạn dùng Integer)
//         Integer studentId = auth.currentUserId() == null ? null : auth.currentUserId().intValue();
//         req.setStudentId(studentId);

//         // 3) đẩy sang service đúng chữ ký (nhận DTO)
//         return service.register(req);
//     }
// }


package tmtd.event.registrations;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tmtd.event.config.AuthFacade;
import tmtd.event.events.EventStatus;
import tmtd.event.events.JpaEvents;
import tmtd.event.registrations.dto.RegistrationCreateRequest;
import tmtd.event.registrations.dto.RegistrationResponse;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class ControllerRegistrationsApi {

    private final JpaEvents events;
    private final ServiceRegistrations service;
    private final AuthFacade auth;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public RegistrationResponse register(@Valid @RequestBody RegistrationCreateRequest req) {
        // 1) kiểm tra event
        var e = events.findById(req.getEventId())
                      .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (e.getStatus() != EventStatus.APPROVED) {
            throw new IllegalStateException("Event not open for registration");
        }

        // 2) gắn studentId từ token (Repo của bạn dùng Integer)
        Integer studentId = auth.currentUserId() == null ? null : auth.currentUserId().intValue();
        req.setStudentId(studentId);

        // 3) đẩy sang service đúng chữ ký (nhận DTO)
        return service.register(req);
    }

    // ===== MODERATION =====

    @PostMapping("/events/{eventId}/registrations/{regId}/approve")
    @PreAuthorize("hasRole('ADMIN') or @authFacade.canModerateEvent(#eventId)")
    public RegistrationResponse approve(@PathVariable Long eventId, @PathVariable Long regId) {
        return service.approveRegistration(eventId, regId, auth.currentUserId());
    }

    @PostMapping("/events/{eventId}/registrations/{regId}/reject")
    @PreAuthorize("hasRole('ADMIN') or @authFacade.canModerateEvent(#eventId)")
    public RegistrationResponse reject(@PathVariable Long eventId, @PathVariable Long regId) {
        return service.rejectRegistration(eventId, regId, auth.currentUserId());
    }
}
