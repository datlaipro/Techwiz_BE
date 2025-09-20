
package tmtd.event.registrations;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;
import org.springframework.http.HttpStatus;

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
        // Đồng nhất Long
        final Long eventId = req.getEventId();
        final Long studentId = req.getStudentId();

        // 1) Event tồn tại?
        EntityEvents event = events.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Event not found: " + eventId));

        // 2) User tồn tại?
        EntityUser user = users.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + studentId));

        // 3) Không cho trùng (chặn sớm)
        if (registrations.existsByEventIdAndStudentId(event.getEventId(), studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
        }

        // 4) Sự kiện phải mở đăng ký
        if (event.getStatus() != tmtd.event.events.EventStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Event is not open for registration");
        }

        // 4.1) Nếu đã từng có bản ghi (ví dụ CANCELLED) → cho đăng ký lại bằng UPDATE
        var existingOpt = registrations.findFirstByEventIdAndStudentId(event.getEventId(), studentId);
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getStatus() == RegistrationStatus.CONFIRMED
                    || existing.getStatus() == RegistrationStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
            }
            // CANCELLED → trừ ghế 1 lần & UPDATE
            int rows = events.tryConsumeSeat(event.getEventId());
            if (rows == 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is full");

            existing.setStatus(RegistrationStatus.CONFIRMED);
            var saved = registrations.save(existing);

            publisher.publishEvent(new RegistrationSucceeded(
                    saved.getRegistrationId(),
                    saved.getEventId(),
                    saved.getStudentId(),
                    user.getEmail(),
                    event.getTitle()));

            return new RegistrationResponse(
                    saved.getRegistrationId(),
                    saved.getEventId(),
                    saved.getStudentId(),
                    saved.getStatus(),
                    saved.getRegisteredOn());
        }

        // 5) Trừ ghế **một lần duy nhất** (atomic)
        int rows = events.tryConsumeSeat(event.getEventId());
        if (rows == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is full");
        }

        // 6) Tạo mới
        EntityRegistrations saved = new EntityRegistrations();
        saved.setEventId(event.getEventId()); // Long
        saved.setStudentId(studentId); // Long
        saved.setStatus(RegistrationStatus.CONFIRMED);
        saved = registrations.save(saved);

        // 7) Publish (AFTER_COMMIT)
        publisher.publishEvent(new RegistrationSucceeded(
                saved.getRegistrationId(),
                saved.getEventId(),
                saved.getStudentId(),
                user.getEmail(),
                event.getTitle()));

        return new RegistrationResponse(
                saved.getRegistrationId(),
                saved.getEventId(),
                saved.getStudentId(),
                saved.getStatus(),
                saved.getRegisteredOn());
    }

    @Override// xử lí lấy ra sự kiện mà người dùng đã đăng kí 
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<tmtd.event.registrations.dto.RegistrationEventItem> listRegisteredEventsOfUser(
            Long studentId,
            tmtd.event.registrations.RegistrationStatus status,
            String whenMode,
            int page,
            int size) {

        // Quy tắc quyền: user chỉ xem được của chính mình; ADMIN thì xem của bất kỳ ai
        // (Nếu AuthFacade của bạn có phương thức lấy userId hiện tại, dùng để kiểm tra)
        Long currentUserId = auth.currentUserId();
        boolean isAdmin = auth.hasRole(tmtd.event.config.Roles.ADMIN);
        if (!isAdmin && (currentUserId == null || !studentId.equals(currentUserId))) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }

        if (whenMode == null || whenMode.isBlank())
            whenMode = "ALL";
        whenMode = whenMode.toUpperCase();
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100));

        return registrations.findUserRegisteredEvents(
                studentId,
                status,
                whenMode,
                java.time.LocalDate.now(),
                pageable);
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
    public List<EntityRegistrations> listByStudent(Long studentId) {
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
                users.findById(reg.getStudentId()).map(EntityUser::getEmail).orElse(null),
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
