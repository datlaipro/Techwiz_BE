package tmtd.event.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tmtd.event.attendance.JpaAttendance;
import tmtd.event.config.AuthFacade;
import tmtd.event.config.Roles;
import tmtd.event.events.dto.EventCreateRequest;
import tmtd.event.events.dto.EventResponse;
import tmtd.event.events.dto.EventStatsResponse;
import tmtd.event.events.dto.EventUpdateRequest;
import tmtd.event.registrations.JpaRegistrations;
import tmtd.event.registrations.RegistrationStatus;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {

    private final JpaEvents repo;
    private final JpaRegistrations registrations;
    private final JpaAttendance attendance;
    private final AuthFacade auth;

    // ===== public
    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> listApproved() {
        return repo.findByStatus(EventStatus.APPROVED).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getApprovedById(Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (e.getStatus() != EventStatus.APPROVED)
            throw forbidden("Event not public");
        return map(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> listApprovedByCategory(String category) {
        String cat = category == null ? "" : category.trim();
        return repo.findByCategoryIgnoreCaseAndStatus(cat, EventStatus.APPROVED)
                .stream()
                .map(this::map)
                .toList();
    }

    // ===== organizer/admin create (cho CHÍNH actor)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Override
    public EventResponse create(EventCreateRequest req) {
        Long actorId = auth.currentUserId();
        var e = new EntityEvents();

        // validate ngày
        if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
            throw bad("startDate must be <= endDate");
        }

        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setCategory(req.category());
        e.setDate(req.date());
        e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
        e.setEndDate(req.endDate() != null ? req.endDate() : req.date());
        e.setTime(req.time());
        e.setVenue(req.venue());
        e.setMainImageUrl(req.mainImageUrl());

        // totalSeats = số ghế còn lại, không âm
        if (req.totalSeats() != null && req.totalSeats() < 0) {
            throw bad("totalSeats must be >= 0");
        }
        e.setTotalSeats(req.totalSeats() == null ? 0L : Math.max(0L, req.totalSeats()));

        if (auth.hasRole(Roles.ADMIN)) {
            e.setOrganizerId(actorId);
            e.setStatus(EventStatus.APPROVED);
            e.setApprovedBy(actorId);
            e.setApprovedAt(Instant.now());
        } else if (auth.hasRole(Roles.ORGANIZER)) {
            e.setOrganizerId(actorId);
            e.setStatus(EventStatus.PENDING_APPROVAL);
        } else {
            throw forbidden("Only ADMIN or ORGANIZER can create events");
        }

        return map(repo.save(e));
    }

    // ===== ADMIN tạo thay organizerId chỉ định
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public EventResponse createAsOrganizer(Long organizerId, EventCreateRequest req) {
        if (!auth.hasRole(Roles.ADMIN)) {
            throw forbidden("Only ADMIN can create on behalf of an organizer");
        }

        var e = new EntityEvents();

        if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
            throw bad("startDate must be <= endDate");
        }

        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setCategory(req.category());
        e.setDate(req.date());
        e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
        e.setEndDate(req.endDate() != null ? req.endDate() : req.date());
        e.setTime(req.time());
        e.setVenue(req.venue());
        e.setMainImageUrl(req.mainImageUrl());

        // totalSeats = số ghế còn lại, không âm
        if (req.totalSeats() != null && req.totalSeats() < 0L) {
            throw bad("totalSeats must be >= 0");
        }
        e.setTotalSeats(req.totalSeats() == null ? 0L : Math.max(0L, req.totalSeats()));

        e.setOrganizerId(organizerId);
        e.setStatus(EventStatus.APPROVED); // rule: admin tạo là duyệt luôn
        e.setApprovedBy(auth.currentUserId());
        e.setApprovedAt(Instant.now());

        return map(repo.save(e));
    }

    // Organizer sửa: chủ sở hữu HOẶC Admin
    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    @Override
    public EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req) {
        var e = mustOwn(organizerId, eventId);
        if (e.getStatus() == EventStatus.APPROVED)
            throw bad("Cannot edit an approved event");

        if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
            throw bad("startDate must be <= endDate");
        }

        if (req.totalSeats() != null && req.totalSeats() < 0L) {
            throw bad("totalSeats must be >= 0");
        }

        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setCategory(req.category());

        e.setDate(req.date());
        e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
        e.setEndDate(req.endDate() != null ? req.endDate() : req.date());

        e.setTime(req.time());
        e.setVenue(req.venue());

        // Chỉ còn một cột `total_seats`: coi như số ghế hiện còn
        if (req.totalSeats() != null) {
            e.setTotalSeats(Math.max(0L, req.totalSeats()));
        }

        e.setMainImageUrl(req.mainImageUrl());
        return map(repo.save(e));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public EventResponse getPendingEventById(Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (e.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw bad("Event is not pending approval");
        }
        return map(e);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Transactional(readOnly = true)
    public EventResponse getPendingEventByIdForOrganizer(Long organizerId, Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));

        if (!auth.hasRole(Roles.ADMIN) && !Objects.equals(e.getOrganizerId(), organizerId)) {
            throw forbidden("Not allowed to view this event");
        }
        if (e.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw bad("Event is not pending approval");
        }

        return map(e);
    }

    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteByEventId(Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));

        if (!auth.hasRole(Roles.ADMIN) && !Objects.equals(e.getOrganizerId(), auth.currentUserId())) {
            throw forbidden("Not owner");
        }
        if (e.getStatus() == EventStatus.APPROVED) {
            throw bad("Cannot delete an approved event");
        }

        int rows = repo.hardDeleteById(eventId);
        if (rows == 0) {
            throw notFound("Event");
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    @Override
    public EventResponse submitForApproval(Long organizerId, Long eventId) {
        var e = mustOwn(organizerId, eventId);
        if (e.getStatus() == EventStatus.APPROVED)
            throw bad("Already approved");
        e.setStatus(EventStatus.PENDING_APPROVAL);
        return map(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> listByOrganizer(Long organizerId) {
        return repo.findByOrganizerId(organizerId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventStatsResponse statsForOrganizer(Long organizerId, Long eventId) {
        var e = mustOwn(organizerId, eventId);

        long total = 0L, pending = 0L, checkedIn = 0L;

        try {
            total = registrations.countByEventId(e.getEventId());
        } catch (Exception ignore) {
        }

        try {
            pending = registrations.countByEventIdAndStatus(e.getEventId(), RegistrationStatus.PENDING);
        } catch (Exception ignore) {
        }

        try {
            checkedIn = attendance.countDistinctAttendees(e.getEventId());
        } catch (Exception ignore) {
        }

        return new EventStatsResponse(e.getEventId(), total, checkedIn, pending);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> listPendingApproveEvents() {
        return repo.findByStatus(EventStatus.PENDING_APPROVAL)
                .stream()
                .map(this::map)
                .toList();
    }

    // ===== admin moderation
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public EventResponse approve(Long adminId, Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (e.getStatus() != EventStatus.PENDING_APPROVAL)
            throw bad("Not pending approval");
        e.setStatus(EventStatus.APPROVED);
        e.setApprovedBy(adminId);
        e.setApprovedAt(Instant.now());
        return map(repo.save(e));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public EventResponse reject(Long adminId, Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (e.getStatus() != EventStatus.PENDING_APPROVAL)
            throw bad("Not pending approval");
        e.setStatus(EventStatus.REJECTED);
        e.setApprovedBy(adminId);
        e.setApprovedAt(Instant.now());
        return map(repo.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatsResponse systemStats() {
        long total = repo.count();
        long approved = repo.findByStatus(EventStatus.APPROVED).size();
        long pending = repo.findByStatus(EventStatus.PENDING_APPROVAL).size();
        long rejected = repo.findByStatus(EventStatus.REJECTED).size();
        return new SystemStatsResponse(total, approved, pending, rejected);
    }

    // ===== helpers
    private EntityEvents mustOwn(Long organizerId, Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (auth.hasRole(Roles.ADMIN)) {
            return e;
        }
        if (!Objects.equals(e.getOrganizerId(), organizerId)) {
            throw forbidden("Not owner");
        }
        return e;
    }

    private EventResponse map(EntityEvents e) {
        // totalSeats hiện được dùng như SỐ GHẾ CÒN LẠI
        Long remaining = e.getTotalSeats() == null ? 0L : e.getTotalSeats();

        return new EventResponse(
                e.getEventId(),
                e.getTitle(),
                e.getDescription(),
                e.getCategory(),
                e.getDate(),
                e.getTime(),
                e.getStartDate(),
                e.getEndDate(),
                e.getVenue(),
                e.getOrganizerId(),
                e.getStatus(),
                e.getApprovedBy(),
                e.getApprovedAt(),
                remaining, // totalSeats (đang coi là ghế còn lại)
                e.getMainImageUrl(),
                remaining // seatsAvailable (giữ tương thích record cũ)
        );
    }

    private RuntimeException notFound(String what) {
        return new IllegalArgumentException(what + " not found");
    }

    private RuntimeException forbidden(String msg) {
        return new SecurityException(msg);
    }

    private RuntimeException bad(String msg) {
        return new IllegalStateException(msg);
    }
}
