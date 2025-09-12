// package tmtd.event.config;

// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import tmtd.event.config.JwtFilter.UserPrincipal;

// @Component
// public class AuthFacade {
//     public Long currentUserId() {
//         Authentication a = SecurityContextHolder.getContext().getAuthentication();
//         if (a == null || a.getPrincipal() == null) return null;
//         if (a.getPrincipal() instanceof UserPrincipal up) return up.userId();
//         return null;
//     }
//     public String currentEmail() {
//         Authentication a = SecurityContextHolder.getContext().getAuthentication();
//         if (a == null || a.getPrincipal() == null) return null;
//         if (a.getPrincipal() instanceof UserPrincipal up) return up.email();
//         return null;
//     }
//     public String currentRawRoles() {
//         Authentication a = SecurityContextHolder.getContext().getAuthentication();
//         if (a == null || a.getPrincipal() == null) return null;
//         if (a.getPrincipal() instanceof UserPrincipal up) return up.roles();
//         return null;
//     }
// }



package tmtd.event.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tmtd.event.config.JwtFilter.UserPrincipal;
import tmtd.event.events.JpaEvents;

@Component("authFacade")
public class AuthFacade {

    private final JpaEvents jpaEvents;

    public AuthFacade(JpaEvents jpaEvents) {
        this.jpaEvents = jpaEvents;
    }

    public Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) return null;
        if (a.getPrincipal() instanceof UserPrincipal up) return up.userId();
        return null;
    }

    public String currentEmail() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) return null;
        if (a.getPrincipal() instanceof UserPrincipal up) return up.email();
        return null;
    }

    public String currentRawRoles() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null) return null;
        if (a.getPrincipal() instanceof UserPrincipal up) return up.roles();
        return null;
    }

    /** Kiểm tra có role (hỗ trợ truyền "ADMIN" hoặc "ROLE_ADMIN") */
    public boolean hasRole(String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return false;
        final String wanted = role.startsWith("ROLE_") ? role : ("ROLE_" + role);
        for (GrantedAuthority ga : a.getAuthorities()) {
            String auth = ga.getAuthority();
            if (wanted.equals(auth) || role.equals(auth)) return true;
        }
        return false;
    }

    /** Chủ sở hữu sự kiện (organizer) */
    public boolean isEventOwner(Long eventId) {
        Long uid = currentUserId();
        return uid != null && eventId != null && jpaEvents.existsByEventIdAndOrganizerId(eventId, uid);
    }

    /** Admin hoặc chính organizer của event */
    public boolean canModerateEvent(Long eventId) {
        return hasRole(Roles.ADMIN) || isEventOwner(eventId);
    }
}
