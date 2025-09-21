
package tmtd.event.registrations;


import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tmtd.event.registrations.dto.RegistrationEventItem; // DTO bạn đã tạo
import tmtd.event.registrations.RegistrationStatus; // nếu chưa import
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tmtd.event.config.AuthFacade;
import tmtd.event.events.EventStatus;
import tmtd.event.events.JpaEvents;
import tmtd.event.registrations.dto.RegistrationCreateRequest;
import tmtd.event.registrations.dto.RegistrationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import tmtd.event.registrations.JpaRegistrations;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class ControllerRegistrationsApi {

      private final JpaRegistrations statsRepo;

    private final JpaEvents events;
    private final ServiceRegistrations service;
    private final AuthFacade auth;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ORGANIZER','ADMIN')") // cho phép tất cả role có quyền đăng kí tham gia sự kiện
                                                            // cho đồng nhất với file security config
    public RegistrationResponse register(@Valid @RequestBody RegistrationCreateRequest req) {
        // 1) kiểm tra event
        var e = events.findById(req.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (e.getStatus() != EventStatus.APPROVED) {
            throw new IllegalStateException("Event not open for registration");
        }

        // 2) gắn studentId từ token (Repo của bạn dùng Integer)
        Long studentId = auth.currentUserId() == null ? null : auth.currentUserId();
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

    @PostMapping("/{regId}/cancel")
    @PreAuthorize("hasAnyRole('USER','ORGANIZER','ADMIN')")
    public RegistrationResponse cancelSelf(@PathVariable Long regId) {
        return service.cancelOwnRegistration(regId, auth.currentUserId());
    }

    @GetMapping("/users/{userId}/events") // api trả về thông tin các sự kiện mà user đã đăng kí
    @PreAuthorize("hasAnyRole('USER','ORGANIZER','ADMIN')")
    public Page<RegistrationEventItem> listUserRegisteredEvents(
            @PathVariable Long userId,
            @RequestParam(required = false) RegistrationStatus status, // PENDING|CONFIRMED|CANCELLED
            @RequestParam(name = "when", required = false) String whenMode, // ALL|UPCOMING|PAST
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Ủy quyền chi tiết đã kiểm trong Service (chỉ xem được của chính mình trừ khi
        // là ADMIN)
        return service.listRegisteredEventsOfUser(userId, status, whenMode, page, size);
    }


}
