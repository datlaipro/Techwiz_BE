
package tmtd.event.registrations;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tmtd.event.config.AuthFacade;
import tmtd.event.config.Roles;
import tmtd.event.events.EntityEvents;
import tmtd.event.events.JpaEvents;
import tmtd.event.registrations.dto.RegistrationCreateRequest;
import tmtd.event.registrations.dto.RegistrationResponse;
import tmtd.event.registrations.events.RegistrationSucceeded;
import tmtd.event.user.EntityUser;
import tmtd.event.user.JpaUser;

@Service
public class ServiceRegistrationsImpl implements ServiceRegistrations {

    private final JpaRegistrations registrations;
    private final JpaEvents events;
    private final JpaUser users;
    private final ApplicationEventPublisher publisher;
    private final AuthFacade auth;

    public ServiceRegistrationsImpl(
            JpaRegistrations registrations,
            JpaEvents events,
            JpaUser users,
            ApplicationEventPublisher publisher,
            AuthFacade auth) {
        this.registrations = registrations;
        this.events = events;
        this.users = users;
        this.publisher = publisher;
        this.auth = auth;
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegistrationCreateRequest req) {
        // Chuẩn hóa kiểu từ DTO
        final Long eventIdLong = Long.valueOf(String.valueOf(req.getEventId())); // int -> Long an toàn
        final Integer studentIdInt = Math.toIntExact(req.getStudentId()); // Long -> Integer

        // 1) Event tồn tại?
        EntityEvents event = events.findById(eventIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + req.getEventId()));

        // 2) User tồn tại?
        EntityUser user = users.findById(studentIdInt) // repo users nhận Integer
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getStudentId()));

        // 3) Không cho trùng
        if (registrations.existsByEventIdAndStudentId(
                Math.toIntExact(event.getEventId()), // Long -> Integer (nếu repo yêu cầu)
                user.getUser_id())) {
            throw new IllegalStateException("Student already registered for this event");
        }

        // 4) Kiểm tra sức chứa (nếu có) -> dùng atomic update, bỏ count CONFIRMED để
        // tránh race
        if (event.getTotalSeats() != null) {
            int updated = events.tryConsumeSeat(event.getEventId());
            if (updated == 0) {
                throw new IllegalStateException("Event is full");
            }
        }

        // 5) Lưu
        EntityRegistrations saved = new EntityRegistrations();
        saved.setEventId(event.getEventId()); // Long
        saved.setStudentId(Long.valueOf(user.getUser_id())); // Integer -> Long
        saved.setStatus(RegistrationStatus.CONFIRMED);
        saved = registrations.save(saved);

        // 6) Publish event (AFTER_COMMIT)
        publisher.publishEvent(new RegistrationSucceeded(
                saved.getRegistrationId(),
                saved.getEventId(),
                Long.valueOf(saved.getStudentId()),
                user.getEmail(),
                event.getTitle()));

        return new RegistrationResponse(
                saved.getRegistrationId(),
                saved.getEventId(),
                Long.valueOf(saved.getStudentId()),
                saved.getStatus(),
                saved.getRegisteredOn());
    }

    @Override
    @Transactional
    public RegistrationResponse cancelOwnRegistration(Long registrationId, Long currentUserId) {
        // Lấy đăng ký
        EntityRegistrations reg = registrations.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + registrationId));

        // Kiểm tra quyền sở hữu (studentId trong bảng là Long)
        if (reg.getStudentId() == null || !reg.getStudentId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this registration");
        }

        // Nếu đã bị CANCELLED rồi thì trả về luôn cho idempotency
        if (reg.getStatus() == RegistrationStatus.CANCELLED) {
            return new RegistrationResponse(
                    reg.getRegistrationId(),
                    reg.getEventId(),
                    Long.valueOf(reg.getStudentId()),
                    reg.getStatus(),
                    reg.getRegisteredOn());
        }

        // Đổi trạng thái và hoàn ghế nếu trước đó CONFIRMED
        RegistrationStatus prev = reg.getStatus();
        reg.setStatus(RegistrationStatus.CANCELLED);
        reg = registrations.save(reg);

        if (prev == RegistrationStatus.CONFIRMED) {
            events.releaseSeat(reg.getEventId());
        }

        return new RegistrationResponse(
                reg.getRegistrationId(),
                reg.getEventId(),
                Long.valueOf(reg.getStudentId()),
                reg.getStatus(),
                reg.getRegisteredOn());
    }

    @Override
    @Transactional(readOnly = true)
    public EntityRegistrations getById(Long id) {
        return registrations.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityRegistrations> listByEvent(Long eventId) {
        return registrations.findAllByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityRegistrations> listByStudent(Integer studentId) {
        return registrations.findAllByStudentId(studentId);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        EntityRegistrations reg = getById(id);
        RegistrationStatus prev = reg.getStatus();
        reg.setStatus(RegistrationStatus.CANCELLED);
        registrations.save(reg);

        // Chỉ hoàn ghế nếu trước đó đã CONFIRMED
        if (prev == RegistrationStatus.CONFIRMED) {
            events.releaseSeat(reg.getEventId());
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, RegistrationStatus status) {
        EntityRegistrations reg = getById(id);
        reg.setStatus(status);
        registrations.save(reg);
    }

    @Override
    @Transactional(readOnly = true)
    public long countConfirmed(Long eventId) {
        return registrations.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }

    // ================== Moderation APIs ==================

    @Override
    @Transactional
    public RegistrationResponse approveRegistration(Long eventId, Long registrationId, Long actorId) {
        // Quyền: Admin hoặc Owner (organizer của event)
        if (!(auth.hasRole(Roles.ADMIN) || auth.isEventOwner(eventId))) {
            throw new AccessDeniedException("Not allowed to approve this registration");
        }

        EntityRegistrations reg = registrations.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        if (!reg.getEventId().equals(eventId)) {
            throw new IllegalStateException("Registration not under this event");
        }

        reg.setStatus(RegistrationStatus.CONFIRMED);
        reg = registrations.save(reg);

        // Gửi thông báo/email/QR nếu cần
        publisher.publishEvent(new RegistrationSucceeded(
                reg.getRegistrationId(),
                reg.getEventId(),
                Long.valueOf(reg.getStudentId()),
                users.findById(reg.getStudentId().intValue()).map(EntityUser::getEmail).orElse(null),
                events.findById(eventId).map(EntityEvents::getTitle).orElse(null)));

        return new RegistrationResponse(
                reg.getRegistrationId(),
                reg.getEventId(),
                Long.valueOf(reg.getStudentId()),
                reg.getStatus(),
                reg.getRegisteredOn());
    }

    @Override
    @Transactional
    public RegistrationResponse rejectRegistration(Long eventId, Long registrationId, Long actorId) {
        if (!(auth.hasRole(Roles.ADMIN) || auth.isEventOwner(eventId))) {
            throw new AccessDeniedException("Not allowed to reject this registration");
        }

        EntityRegistrations reg = registrations.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        if (!reg.getEventId().equals(eventId)) {
            throw new IllegalStateException("Registration not under this event");
        }

        RegistrationStatus prev = reg.getStatus();
        reg.setStatus(RegistrationStatus.CANCELLED);
        reg = registrations.save(reg);

        // Nếu đang hoàn ghế khi hủy
        if (prev == RegistrationStatus.CONFIRMED) {
            events.releaseSeat(eventId);
        }

        return new RegistrationResponse(
                reg.getRegistrationId(),
                reg.getEventId(),
                Long.valueOf(reg.getStudentId()),
                reg.getStatus(),
                reg.getRegisteredOn());
    }
}
