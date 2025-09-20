package tmtd.event.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmtd.event.user.ServiceUser;
import tmtd.event.user.dto.AdminCreateUserRequest;
import tmtd.event.user.dto.UserResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final ServiceUser userService;

    // Admin tạo organizer mới
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createOrganizer(@Valid @RequestBody AdminCreateUserRequest req){
        var created = userService.adminCreateOrganizer(req);
        return ResponseEntity.created(URI.create("/api/admin/users/" + created.userId())).body(created);
    }

    // Admin nâng cấp user hiện có thành organizer
    @PostMapping("/{id}/roles/organizer")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse grantOrganizer(@PathVariable Long id) { // ⬅ đổi Long -> Integer
        return userService.addRole(id, "ROLE_ORGANIZER");
    }
}
