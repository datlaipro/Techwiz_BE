


// package tmtd.event.events;

// import java.time.Instant;
// import java.util.List;
// import java.util.Objects;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import lombok.RequiredArgsConstructor;
// import tmtd.event.attendance.JpaAttendance;
// import tmtd.event.config.AuthFacade;
// import tmtd.event.config.Roles;
// import tmtd.event.events.dto.EventCreateRequest;
// import tmtd.event.events.dto.EventResponse;
// import tmtd.event.events.dto.EventStatsResponse;
// import tmtd.event.events.dto.EventUpdateRequest;
// import tmtd.event.registrations.JpaRegistrations;
// import tmtd.event.registrations.RegistrationStatus;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class EventsServiceImpl implements EventsService {

//     private final JpaEvents repo;
//     private final JpaRegistrations registrations; // <- inject thêm
//     private final JpaAttendance attendance;       // <- inject thêm
//     private final AuthFacade auth;                // ⬅️ thêm

//     // ===== public
//     @Override
//     @Transactional(readOnly = true)
//     public List<EventResponse> listApproved() {
//         return repo.findByStatus(EventStatus.APPROVED).stream().map(this::map).toList();
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public EventResponse getApprovedById(Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         if (e.getStatus() != EventStatus.APPROVED)
//             throw forbidden("Event not public");
//         return map(e);
//     }

//     // ===== organizer/admin create (cho CHÍNH actor)
//     @Override
//     public EventResponse create(EventCreateRequest req) {
//         Long actorId = auth.currentUserId();
//         var e = new EntityEvents();

//         // validate ngày
//         if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
//             throw bad("startDate must be <= endDate");
//         }

//         e.setTitle(req.title());
//         e.setDescription(req.description());
//         e.setCategory(req.category());
//         e.setDate(req.date());
//         e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
//         e.setEndDate(req.endDate() != null ? req.endDate() : req.date());
//         e.setTime(req.time());
//         e.setVenue(req.venue());
//         e.setTotalSeats(req.totalSeats());
//         e.setMainImageUrl(req.mainImageUrl());

//         if (auth.hasRole(Roles.ADMIN)) {
//             // Admin tạo CHO CHÍNH MÌNH ở endpoint này
//             e.setOrganizerId(actorId);
//             e.setStatus(EventStatus.APPROVED);
//             e.setApprovedBy(actorId);
//             e.setApprovedAt(Instant.now());
//         } else if (auth.hasRole(Roles.ORGANIZER)) {
//             // Organizer tạo: luôn DRAFT (submit sau)
//             e.setOrganizerId(actorId);
//             e.setStatus(EventStatus.DRAFT);
//         } else {
//             throw forbidden("Only ADMIN or ORGANIZER can create events");
//         }

//         if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
//             throw bad("totalSeats must be >= 0");
//         }

//         return map(repo.save(e));
//     }

//     @Override
// @Transactional(readOnly = true)
// public List<EventResponse> listApprovedByCategory(String category) {
//     String cat = category == null ? "" : category.trim();
//     return repo.findByCategoryIgnoreCaseAndStatus(cat, EventStatus.APPROVED)
//                .stream()
//                .map(this::map)
//                .toList();
// }

//     // ===== ADMIN tạo thay organizerId chỉ định
//     @Override
//     public EventResponse createAsOrganizer(Long organizerId, EventCreateRequest req) {
//         if (!auth.hasRole(Roles.ADMIN)) {
//             throw forbidden("Only ADMIN can create on behalf of an organizer");
//         }

//         var e = new EntityEvents();

//         if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
//             throw bad("startDate must be <= endDate");
//         }

//         e.setTitle(req.title());
//         e.setDescription(req.description());
//         e.setCategory(req.category());
//         e.setDate(req.date());
//         e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
//         e.setEndDate(req.endDate() != null ? req.endDate() : req.date());
//         e.setTime(req.time());
//         e.setVenue(req.venue());
//         e.setTotalSeats(req.totalSeats());
//         e.setMainImageUrl(req.mainImageUrl());

//         e.setOrganizerId(organizerId);            // gán đúng người được tạo thay
//         e.setStatus(EventStatus.APPROVED);        // rule: admin tạo là duyệt luôn
//         e.setApprovedBy(auth.currentUserId());
//         e.setApprovedAt(Instant.now());

//         if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
//             throw bad("totalSeats must be >= 0");
//         }

//         return map(repo.save(e));
//     }

//     @Override
//     public EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Cannot edit an approved event");

//         if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
//             throw bad("startDate must be <= endDate");
//         }

//         e.setTitle(req.title());
//         e.setDescription(req.description());
//         e.setCategory(req.category());

//         e.setDate(req.date());
//         e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
//         e.setEndDate(req.endDate() != null ? req.endDate() : req.date());

//         e.setTime(req.time());
//         e.setVenue(req.venue());
//         e.setTotalSeats(req.totalSeats());
//         e.setMainImageUrl(req.mainImageUrl());
//         return map(repo.save(e));
//     }

//     @Override
//     public void deleteByOwner(Long organizerId, Long eventId) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Cannot delete an approved event");
//         repo.delete(e);
//     }

//     @Override
//     public EventResponse submitForApproval(Long organizerId, Long eventId) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Already approved");
//         e.setStatus(EventStatus.PENDING_APPROVAL);
//         return map(repo.save(e));
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<EventResponse> listByOrganizer(Long organizerId) {
//         return repo.findByOrganizerId(organizerId).stream().map(this::map).toList();
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public EventStatsResponse statsForOrganizer(Long organizerId, Long eventId) {
//         var e = mustOwn(organizerId, eventId);

//         long total = 0L, pending = 0L, checkedIn = 0L;

//         try {
//             total = registrations.countByEventId(e.getEventId());
//         } catch (Exception ignore) {
//         }

//         try {
//             pending = registrations.countByEventIdAndStatus(e.getEventId(), RegistrationStatus.PENDING);
//         } catch (Exception ignore) {
//         }

//         try {
//             // tuỳ bạn đã chọn JPQL hay derived method trong JpaAttendance
//             checkedIn = attendance.countDistinctAttendees(e.getEventId());
//             // hoặc: checkedIn = attendance.countByEventId(e.getEventId());
//         } catch (Exception ignore) {
//         }

//         return new EventStatsResponse(e.getEventId(), total, checkedIn, pending);
//     }

//     // ===== admin moderation
//     @Override
//     public EventResponse approve(Long adminId, Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         if (e.getStatus() != EventStatus.PENDING_APPROVAL)
//             throw bad("Not pending approval");
//         e.setStatus(EventStatus.APPROVED);
//         e.setApprovedBy(adminId);
//         e.setApprovedAt(Instant.now());
//         return map(repo.save(e));
//     }

//     @Override
//     public EventResponse reject(Long adminId, Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         if (e.getStatus() != EventStatus.PENDING_APPROVAL)
//             throw bad("Not pending approval");
//         e.setStatus(EventStatus.REJECTED);
//         e.setApprovedBy(adminId);
//         e.setApprovedAt(Instant.now());
//         return map(repo.save(e));
//     }

//     // ===== system stats
//     @Override
//     @Transactional(readOnly = true)
//     public SystemStatsResponse systemStats() {
//         long total = repo.count();
//         long approved = repo.findByStatus(EventStatus.APPROVED).size();
//         long pending = repo.findByStatus(EventStatus.PENDING_APPROVAL).size();
//         long rejected = repo.findByStatus(EventStatus.REJECTED).size();
//         return new SystemStatsResponse(total, approved, pending, rejected);
//     }

//     // ===== helpers
//     private EntityEvents mustOwn(Long organizerId, Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         if (!Objects.equals(e.getOrganizerId(), organizerId))
//             throw forbidden("Not owner");
//         return e;
//     }

//     private EventResponse map(EntityEvents e) {
//         return new EventResponse(
//             e.getEventId(),
//             e.getTitle(),
//             e.getDescription(),
//             e.getCategory(),
//             e.getDate(),
//             e.getTime(),        // time đứng ngay sau date (khớp record)
//             e.getStartDate(),
//             e.getEndDate(),
//             e.getVenue(),
//             e.getOrganizerId(),
//             e.getStatus(),
//             e.getApprovedBy(),
//             e.getApprovedAt(),
//             e.getTotalSeats(),
//             e.getMainImageUrl()
//         );
//     }

//     private RuntimeException notFound(String what) {
//         return new IllegalArgumentException(what + " not found");
//     }

//     private RuntimeException forbidden(String msg) {
//         return new SecurityException(msg);
//     }

//     private RuntimeException bad(String msg) {
//         return new IllegalStateException(msg);
//     }
// }



package tmtd.event.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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

@Service
@RequiredArgsConstructor
@Transactional
public class EventsServiceImpl implements EventsService {

    private final JpaEvents repo;
    private final JpaRegistrations registrations; // <- inject thêm
    private final JpaAttendance attendance;       // <- inject thêm
    private final AuthFacade auth;                // ⬅️ thêm

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

    // ⬇️ BỔ SUNG: lọc theo danh mục, chỉ lấy APPROVED
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
        e.setTotalSeats(req.totalSeats());
        e.setMainImageUrl(req.mainImageUrl());

        if (auth.hasRole(Roles.ADMIN)) {
            // Admin tạo CHO CHÍNH MÌNH ở endpoint này
            e.setOrganizerId(actorId);
            e.setStatus(EventStatus.APPROVED);
            e.setApprovedBy(actorId);
            e.setApprovedAt(Instant.now());
        } else if (auth.hasRole(Roles.ORGANIZER)) {
            // Organizer tạo: luôn DRAFT (submit sau)
            e.setOrganizerId(actorId);
            e.setStatus(EventStatus.DRAFT);
        } else {
            throw forbidden("Only ADMIN or ORGANIZER can create events");
        }

        if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
            throw bad("totalSeats must be >= 0");
        }

        return map(repo.save(e));
    }

    // ===== ADMIN tạo thay organizerId chỉ định
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
        e.setTotalSeats(req.totalSeats());
        e.setMainImageUrl(req.mainImageUrl());

        e.setOrganizerId(organizerId);            // gán đúng người được tạo thay
        e.setStatus(EventStatus.APPROVED);        // rule: admin tạo là duyệt luôn
        e.setApprovedBy(auth.currentUserId());
        e.setApprovedAt(Instant.now());

        if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
            throw bad("totalSeats must be >= 0");
        }

        return map(repo.save(e));
    }

    @Override
    public EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req) {
        var e = mustOwn(organizerId, eventId);
        if (e.getStatus() == EventStatus.APPROVED)
            throw bad("Cannot edit an approved event");

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
        e.setTotalSeats(req.totalSeats());
        e.setMainImageUrl(req.mainImageUrl());
        return map(repo.save(e));
    }

    @Override
    public void deleteByOwner(Long organizerId, Long eventId) {
        var e = mustOwn(organizerId, eventId);
        if (e.getStatus() == EventStatus.APPROVED)
            throw bad("Cannot delete an approved event");
        repo.delete(e);
    }

    @Override
    public EventResponse submitForApproval(Long organizerId, Long eventId) {
        var e = mustOwn(organizerId, eventId);
        if (e.getStatus() == EventStatus.APPROVED)
            throw bad("Already approved");
        e.setStatus(EventStatus.PENDING_APPROVAL);
        return map(repo.save(e));
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
            // tuỳ bạn đã chọn JPQL hay derived method trong JpaAttendance
            checkedIn = attendance.countDistinctAttendees(e.getEventId());
            // hoặc: checkedIn = attendance.countByEventId(e.getEventId());
        } catch (Exception ignore) {
        }

        return new EventStatsResponse(e.getEventId(), total, checkedIn, pending);
    }

    // ⬇️ BỔ SUNG: danh sách sự kiện đang chờ duyệt
    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> listPendingApproveEvents() {
        return repo.findByStatus(EventStatus.PENDING_APPROVAL)
                   .stream()
                   .map(this::map)
                   .toList();
    }

    // ===== admin moderation
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

    // ===== system stats
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
        if (!Objects.equals(e.getOrganizerId(), organizerId))
            throw forbidden("Not owner");
        return e;
    }

    private EventResponse map(EntityEvents e) {
        return new EventResponse(
            e.getEventId(),
            e.getTitle(),
            e.getDescription(),
            e.getCategory(),
            e.getDate(),
            e.getTime(),        // time đứng ngay sau date (khớp record)
            e.getStartDate(),
            e.getEndDate(),
            e.getVenue(),
            e.getOrganizerId(),
            e.getStatus(),
            e.getApprovedBy(),
            e.getApprovedAt(),
            e.getTotalSeats(),
            e.getMainImageUrl()
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
