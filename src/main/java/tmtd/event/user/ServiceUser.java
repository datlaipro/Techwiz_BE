package tmtd.event.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmtd.event.user.dto.AdminCreateUserRequest;
import tmtd.event.user.dto.UserResponse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Service
@Transactional
public class ServiceUser {

    private final JpaUser jpaUser;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ServiceUser(JpaUser jpaUser, PasswordEncoder passwordEncoder) {
        this.jpaUser = jpaUser;
        this.passwordEncoder = passwordEncoder;
    }

    // ----------------- Helpers -----------------
    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean looksLikeUniqueEmailViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        if (msg == null) msg = "";
        msg = msg.toLowerCase();
        // tùy DB/driver: tên constraint hoặc từ khóa duplicate/unique
        return msg.contains("uk_entity_user_email") || msg.contains("duplicate") || msg.contains("unique");
    }

    // ----------------- Public register -----------------
    public EntityUser saveEntityUser(EntityUser entityUser) {
        // Chuẩn hóa & mặc định
        String email = normalizeEmail(entityUser.getEmail());
        entityUser.setEmail(email);
        entityUser.setDateJoin(new java.sql.Date(System.currentTimeMillis()));

        if (!isBlank(entityUser.getPassword())) {
            entityUser.setPassword(passwordEncoder.encode(entityUser.getPassword()));
        }
        if (isBlank(entityUser.getRoles())) {
            entityUser.setRoles("ROLE_USER");
        }

        // Chặn trùng email: check sớm + bắt ở DB
        if (!isBlank(email) && jpaUser.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        try {
            return jpaUser.save(entityUser);
        } catch (DataIntegrityViolationException ex) {
            if (looksLikeUniqueEmailViolation(ex)) {
                throw new EmailAlreadyUsedException(email);
            }
            throw ex;
        }
    }

    // ----------------- Password utils -----------------
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ----------------- Queries -----------------
    public Optional<EntityUser> findByEmail(String email) {
        String norm = normalizeEmail(email);
        // Ưu tiên ignore-case; fallback nếu bạn chưa thêm method ở JpaUser
        try {
            return jpaUser.findByEmailIgnoreCase(norm);
        } catch (Throwable t) {
            return jpaUser.findByEmail(norm);
        }
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

    // ----------------- Admin ops -----------------
    // ADMIN tạo organizer mới
    public UserResponse adminCreateOrganizer(AdminCreateUserRequest req) {
        String email = normalizeEmail(req.email());

        if (jpaUser.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        var u = new EntityUser();
        u.setEmail(email);
        u.setFullName(req.fullName());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRoles("ROLE_USER,ROLE_ORGANIZER");
        u.setDateJoin(new java.sql.Date(System.currentTimeMillis()));

        try {
            var saved = jpaUser.save(u);
            Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());
            return new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
        } catch (DataIntegrityViolationException ex) {
            if (looksLikeUniqueEmailViolation(ex)) {
                throw new EmailAlreadyUsedException(email);
            }
            throw ex;
        }
    }

    // ADMIN nâng cấp role cho user hiện có
    public UserResponse addRole(Integer userId, String role) {
        var u = jpaUser.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String roles = isBlank(u.getRoles()) ? "" : u.getRoles();
        var set = new LinkedHashSet<>(
                Arrays.stream(roles.split(","))
                      .map(String::trim)
                      .filter(s -> !s.isEmpty())
                      .toList()
        );
        set.add(role);
        u.setRoles(String.join(",", set));

        var saved = jpaUser.save(u);
        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());
        return new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
    }
}
