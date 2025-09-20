
package tmtd.event.config;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import tmtd.event.config.JwtFilter.UserPrincipal;
import tmtd.event.events.JpaEvents;

@Component("auth") // 👈 quan trọng: dùng @auth.* trong SpEL
public class AuthFacade {

    private final JpaEvents jpaEvents;

    public AuthFacade(JpaEvents jpaEvents) {
        this.jpaEvents = jpaEvents;
    }

    /** ID người dùng hiện tại từ JWT (UserPrincipal) */
    public Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null)
            return null;
        if (a.getPrincipal() instanceof UserPrincipal up)
            return up.userId();
        return null;
    }

    /** Email hiện tại (nếu có trong UserPrincipal) */
    public String currentEmail() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null)
            return null;
        if (a.getPrincipal() instanceof UserPrincipal up)
            return up.email();
        return null;
    }

    /** Chuỗi raw roles từ UserPrincipal (nếu bạn cần debug) */
    public String currentRawRoles() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null)
            return null;
        if (a.getPrincipal() instanceof UserPrincipal up)
            return up.roles();
        return null;
    }

    /** Kiểm tra có role (hỗ trợ truyền "ADMIN" hoặc "ROLE_ADMIN") */
    public boolean hasRole(String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            return false;

        final String wanted = role.startsWith("ROLE_") ? role : ("ROLE_" + role);
        for (GrantedAuthority ga : a.getAuthorities()) {
            String auth = ga.getAuthority();
            if (Objects.equals(auth, wanted) || Objects.equals(auth, role)) {
                return true;
            }
        }
        return false;
    }

    /** Chủ sở hữu sự kiện (organizer) — tên gốc bạn đang dùng */
    public boolean isEventOwner(Long eventId) {
        Long uid = currentUserId();
        return uid != null && eventId != null
                && jpaEvents.existsByEventIdAndOrganizerId(eventId, uid);
    }

    /** Alias để tương thích với chỗ khác có thể gọi isOwnerOfEvent(...) */
    public boolean isOwnerOfEvent(Long eventId) {
        return isEventOwner(eventId);
    }

    /** Admin hoặc chính organizer của event */
    public boolean canModerateEvent(Long eventId) {
        return hasRole(Roles.ADMIN) || isEventOwner(eventId);
    }
}
