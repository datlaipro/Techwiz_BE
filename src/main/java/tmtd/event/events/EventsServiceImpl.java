


// package tmtd.event.events;

// import java.time.Instant;
// import java.util.List;
// import java.util.Objects;

// import org.springframework.security.access.prepost.PreAuthorize;
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
//     private final JpaAttendance attendance; // <- inject thêm
//     private final AuthFacade auth; // ⬅️ thêm

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

//     // ⬇️ Lọc theo danh mục, chỉ lấy APPROVED
//     @Override
//     @Transactional(readOnly = true)
//     public List<EventResponse> listApprovedByCategory(String category) {
//         String cat = category == null ? "" : category.trim();
//         return repo.findByCategoryIgnoreCaseAndStatus(cat, EventStatus.APPROVED)
//                 .stream()
//                 .map(this::map)
//                 .toList();
//     }

//     // ===== organizer/admin create (cho CHÍNH actor)
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
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
//             e.setStatus(EventStatus.PENDING_APPROVAL);
//         } else {
//             throw forbidden("Only ADMIN or ORGANIZER can create events");
//         }

//         if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
//             throw bad("totalSeats must be >= 0");
//         }

//         // ✅ Khởi tạo seatsAvailable theo totalSeats
//         if (e.getTotalSeats() == null) {
//             e.setSeatsAvailable(0);
//         } else {
//             e.setSeatsAvailable(Math.max(0, e.getTotalSeats()));
//         }

//         return map(repo.save(e));
//     }

//     // ===== ADMIN tạo thay organizerId chỉ định
//     @PreAuthorize("hasRole('ADMIN')")
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

//         e.setOrganizerId(organizerId); // gán đúng người được tạo thay
//         e.setStatus(EventStatus.APPROVED); // rule: admin tạo là duyệt luôn
//         e.setApprovedBy(auth.currentUserId());
//         e.setApprovedAt(Instant.now());

//         if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
//             throw bad("totalSeats must be >= 0");
//         }

//         // ✅ Khởi tạo seatsAvailable theo totalSeats
//         if (e.getTotalSeats() == null) {
//             e.setSeatsAvailable(0);
//         } else {
//             e.setSeatsAvailable(Math.max(0, e.getTotalSeats()));
//         }

//         return map(repo.save(e));
//     }

//     // Organizer sửa: chủ sở hữu HOẶC Admin
//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     @Override
//     public EventResponse updateByOwner(Long organizerId, Long eventId, EventUpdateRequest req) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Cannot edit an approved event");

//         if (req.startDate() != null && req.endDate() != null && req.startDate().isAfter(req.endDate())) {
//             throw bad("startDate must be <= endDate");
//         }

//         if (req.totalSeats() != null && req.totalSeats() < 0) {
//             throw bad("totalSeats must be >= 0");
//         }

//         e.setTitle(req.title());
//         e.setDescription(req.description());
//         e.setCategory(req.category());

//         e.setDate(req.date());
//         e.setStartDate(req.startDate() != null ? req.startDate() : req.date());
//         e.setEndDate(req.endDate() != null ? req.endDate() : req.date());

//         e.setTime(req.time());
//         e.setVenue(req.venue());

//         // ✅ cập nhật totalSeats & clamp seatsAvailable
//         e.setTotalSeats(req.totalSeats());
//         if (e.getTotalSeats() == null) {
//             e.setSeatsAvailable(0);
//         } else {
//             e.setSeatsAvailable(Math.min(
//                     Math.max(0, e.getSeatsAvailable() == null ? 0 : e.getSeatsAvailable()),
//                     e.getTotalSeats()));
//         }

//         e.setMainImageUrl(req.mainImageUrl());
//         return map(repo.save(e));
//     }

//     @Override
//     @PreAuthorize("hasRole('ADMIN')")
//     @Transactional(readOnly = true)
//     public EventResponse getPendingEventById(Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         if (e.getStatus() != EventStatus.PENDING_APPROVAL) {
//             throw bad("Event is not pending approval");
//         }
//         return map(e);
//     }

//     @Override
//     @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
//     @Transactional(readOnly = true)
//     public EventResponse getPendingEventByIdForOrganizer(Long organizerId, Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));

//         // Organizer chỉ được xem sự kiện của chính mình
//         if (!auth.hasRole(Roles.ADMIN) && !Objects.equals(e.getOrganizerId(), organizerId)) {
//             throw forbidden("Not allowed to view this event");
//         }

//         if (e.getStatus() != EventStatus.PENDING_APPROVAL) {
//             throw bad("Event is not pending approval");
//         }

//         return map(e);
//     }

//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     @Override
//     public void deleteByOwner(Long organizerId, Long eventId) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Cannot delete an approved event");
//         repo.delete(e);
//     }

//     @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
//     @Override
//     public EventResponse submitForApproval(Long organizerId, Long eventId) {
//         var e = mustOwn(organizerId, eventId);
//         if (e.getStatus() == EventStatus.APPROVED)
//             throw bad("Already approved");
//         e.setStatus(EventStatus.PENDING_APPROVAL);
//         return map(e);
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

//     // ⬇️ Danh sách sự kiện đang chờ duyệt
//     @Override
//     @Transactional(readOnly = true)
//     public List<EventResponse> listPendingApproveEvents() {
//         return repo.findByStatus(EventStatus.PENDING_APPROVAL)
//                 .stream()
//                 .map(this::map)
//                 .toList();
//     }

//     // ===== admin moderation
//     @PreAuthorize("hasRole('ADMIN')")
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

//     @PreAuthorize("hasRole('ADMIN')")
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
//     /**
//      * BẮT BUỘC: Cho phép ADMIN bypass kiểm tra chủ sở hữu để khớp
//      * với @PreAuthorize.
//      */
//     private EntityEvents mustOwn(Long organizerId, Long eventId) {
//         var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
//         // ✅ ADMIN bypass: nếu đã được authorize ở annotation, không chặn thêm ở đây
//         if (auth.hasRole(Roles.ADMIN)) {
//             return e;
//         }
//         if (!Objects.equals(e.getOrganizerId(), organizerId)) {
//             throw forbidden("Not owner");
//         }
//         return e;
//     }

//     private EventResponse map(EntityEvents e) {
//         // 🔔 NHỚ cập nhật EventResponse(record/class) để có thêm trường seatsAvailable
//         return new EventResponse(
//                 e.getEventId(),
//                 e.getTitle(),
//                 e.getDescription(),
//                 e.getCategory(),
//                 e.getDate(),
//                 e.getTime(),
//                 e.getStartDate(),
//                 e.getEndDate(),
//                 e.getVenue(),
//                 e.getOrganizerId(),
//                 e.getStatus(),
//                 e.getApprovedBy(),
//                 e.getApprovedAt(),
//                 e.getTotalSeats(),
//                 e.getMainImageUrl(),
//                 e.getSeatsAvailable() // <-- thêm trường này để FE hiển thị
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
    private final JpaRegistrations registrations; // <- inject thêm
    private final JpaAttendance attendance; // <- inject thêm
    private final AuthFacade auth; // ⬅️ thêm

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

    // ⬇️ Lọc theo danh mục, chỉ lấy APPROVED
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
            e.setStatus(EventStatus.PENDING_APPROVAL);
        } else {
            throw forbidden("Only ADMIN or ORGANIZER can create events");
        }

        if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
            throw bad("totalSeats must be >= 0");
        }

        // ✅ Khởi tạo seatsAvailable theo totalSeats
        if (e.getTotalSeats() == null) {
            e.setSeatsAvailable(0);
        } else {
            e.setSeatsAvailable(Math.max(0, e.getTotalSeats()));
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
        e.setTotalSeats(req.totalSeats());
        e.setMainImageUrl(req.mainImageUrl());

        e.setOrganizerId(organizerId); // gán đúng người được tạo thay
        e.setStatus(EventStatus.APPROVED); // rule: admin tạo là duyệt luôn
        e.setApprovedBy(auth.currentUserId());
        e.setApprovedAt(Instant.now());

        if (e.getTotalSeats() != null && e.getTotalSeats() < 0) {
            throw bad("totalSeats must be >= 0");
        }

        // ✅ Khởi tạo seatsAvailable theo totalSeats
        if (e.getTotalSeats() == null) {
            e.setSeatsAvailable(0);
        } else {
            e.setSeatsAvailable(Math.max(0, e.getTotalSeats()));
        }

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

        if (req.totalSeats() != null && req.totalSeats() < 0) {
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

        // ✅ cập nhật totalSeats & clamp seatsAvailable
        e.setTotalSeats(req.totalSeats());
        if (e.getTotalSeats() == null) {
            e.setSeatsAvailable(0);
        } else {
            e.setSeatsAvailable(Math.min(
                    Math.max(0, e.getSeatsAvailable() == null ? 0 : e.getSeatsAvailable()),
                    e.getTotalSeats()));
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

        // Organizer chỉ được xem sự kiện của chính mình
        if (!auth.hasRole(Roles.ADMIN) && !Objects.equals(e.getOrganizerId(), organizerId)) {
            throw forbidden("Not allowed to view this event");
        }

        if (e.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw bad("Event is not pending approval");
        }

        return map(e);
    }

    // @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    // @Override
    // @Transactional(readOnly = true)
    // public void deleteByOwner(Long organizerId, Long eventId) {
    // var e = mustOwn(organizerId, eventId);
    // if (e.getStatus() == EventStatus.APPROVED)
    // throw bad("Cannot delete an approved event");
    // repo.delete(e);
    // }

    @PreAuthorize("hasRole('ADMIN') or @auth.isOwnerOfEvent(#eventId)")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // ⚡ thoát mọi outer read-only
    public void deleteByEventId(Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));

        // ✅ Cho ADMIN bypass; còn lại xác nhận đúng chủ
        if (!auth.hasRole(Roles.ADMIN) && !Objects.equals(e.getOrganizerId(), auth.currentUserId())) {
            throw forbidden("Not owner");
        }
        if (e.getStatus() == EventStatus.APPROVED) {
            throw bad("Cannot delete an approved event");
        }

        // ⚡ Bulk delete + auto-flush để chắc chắn phát lệnh DELETE
        int rows = repo.hardDeleteById(eventId); // phương thức ở JpaEvents bên dưới
        if (rows == 0) {
            // Nếu ai đó đã xóa trước đó
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
            // tuỳ bạn đã chọn JPQL hay derived method trong JpaAttendance
            checkedIn = attendance.countDistinctAttendees(e.getEventId());
            // hoặc: checkedIn = attendance.countByEventId(e.getEventId());
        } catch (Exception ignore) {
        }

        return new EventStatsResponse(e.getEventId(), total, checkedIn, pending);
    }

    // ⬇️ Danh sách sự kiện đang chờ duyệt
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
    /**
     * BẮT BUỘC: Cho phép ADMIN bypass kiểm tra chủ sở hữu để khớp
     * với @PreAuthorize.
     */
    private EntityEvents mustOwn(Long organizerId, Long eventId) {
        var e = repo.findById(eventId).orElseThrow(() -> notFound("Event"));
        // ✅ ADMIN bypass: nếu đã được authorize ở annotation, không chặn thêm ở đây
        if (auth.hasRole(Roles.ADMIN)) {
            return e;
        }
        if (!Objects.equals(e.getOrganizerId(), organizerId)) {
            throw forbidden("Not owner");
        }
        return e;
    }

    private EventResponse map(EntityEvents e) {
        // 🔔 NHỚ cập nhật EventResponse(record/class) để có thêm trường seatsAvailable
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
                e.getTotalSeats(),
                e.getMainImageUrl(),
                e.getSeatsAvailable() // <-- thêm trường này để FE hiển thị
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
