


package tmtd.event.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tmtd.event.config.AuthFacade;
import tmtd.event.events.EventsService;
import tmtd.event.events.EventsService.SystemStatsResponse;
import tmtd.event.events.dto.EventResponse;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminController {

    private final EventsService events;
    private final AuthFacade auth;

    /** Duyệt sự kiện (Admin) */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse approve(@PathVariable Long id) {
        return events.approve(auth.currentUserId(), id);
    }

    /** Từ chối sự kiện (Admin) */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse reject(@PathVariable Long id) {
        return events.reject(auth.currentUserId(), id);
    }

    /** Thống kê hệ thống */
    @GetMapping("/stats/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public SystemStatsResponse overview() {
        return events.systemStats();
    }
}



