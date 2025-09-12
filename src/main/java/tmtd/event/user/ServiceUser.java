package tmtd.event.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tmtd.event.user.dto.UserResponse;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceUser {
    private final JpaUser jpaUser;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ServiceUser(JpaUser jpaUser, PasswordEncoder passwordEncoder) {
        this.jpaUser = jpaUser;
        this.passwordEncoder = passwordEncoder;
    }

    public EntityUser saveEntityUser(EntityUser entityUser) {
        // dateJoin đang là java.sql.Date trong EntityUser => set đúng kiểu:
        entityUser.setDateJoin(new java.sql.Date(System.currentTimeMillis()));

        if (entityUser.getPassword() != null && !entityUser.getPassword().isEmpty()) {
            entityUser.setPassword(passwordEncoder.encode(entityUser.getPassword()));
        }
        if (entityUser.getRoles() == null || entityUser.getRoles().isEmpty()) {
            entityUser.setRoles("ROLE_USER");
        }
        return jpaUser.save(entityUser);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Optional<EntityUser> findByEmail(String email) {
        return jpaUser.findByEmail(email);
    }

    public Page<EntityUser> findAll(Pageable pageable) {
        return jpaUser.findAll(pageable);
    }

    public List<EntityUser> findAll() {
        return jpaUser.findAll();
    }

    // Repo của bạn đang dùng Integer id => đồng bộ kiểu int/Integer
    public Optional<EntityUser> findById(int id) {
        return jpaUser.findById(id);
    }

    public void deleteById(int id) {
        jpaUser.deleteById(id);
    }

    public boolean existsById(int id) {
        return jpaUser.existsById(id);
    }

    public List<EntityUser> findByRoles(String roles) {
        return jpaUser.findByRolesContaining(roles);
    }

    // --- ADMIN tạo organizer mới ---
    public tmtd.event.user.dto.UserResponse adminCreateOrganizer(tmtd.event.user.dto.AdminCreateUserRequest req) {
        var u = new tmtd.event.user.EntityUser();
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRoles("ROLE_USER,ROLE_ORGANIZER"); // ép cứng
        // EntityUser.dateJoin là java.sql.Date:
        u.setDateJoin(new java.sql.Date(System.currentTimeMillis()));

        var saved = jpaUser.save(u);

        // Nếu UserResponse.userId là Long, mà saved.getUser_id() là Integer -> convert:
        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());
        return new tmtd.event.user.dto.UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
    }

    // --- ADMIN nâng cấp role cho user hiện có ---
    public tmtd.event.user.dto.UserResponse addRole(Integer userId, String role) { // dùng Integer cho khớp Repo
        var u = jpaUser.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        var roles = (u.getRoles() == null || u.getRoles().isBlank()) ? "" : u.getRoles();
        var list = new java.util.LinkedHashSet<>(java.util.Arrays.stream(roles.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList());
        list.add(role);
        u.setRoles(String.join(",", list));

        var saved = jpaUser.save(u);

        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());
        return new tmtd.event.user.dto.UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
    }
}
