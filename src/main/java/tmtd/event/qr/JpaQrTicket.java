package tmtd.event.qr;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JpaQrTicket extends JpaRepository<EntityQrTicket, Long> {

    // === Lookups cơ bản ===
    Optional<EntityQrTicket> findByToken(String token);

    Optional<EntityQrTicket> findByEventIdAndStudentId(Long eventId, Long studentId);

    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);

    // Vé ACTIVE trước (phục vụ idempotent)
    Optional<EntityQrTicket> findFirstByEventIdAndStudentIdAndStatus(
            Long eventId,
            Long studentId,
            EntityQrTicket.TicketStatus status);

    // Fallback nếu không quan tâm trạng thái
    Optional<EntityQrTicket> findFirstByEventIdAndStudentId(Long eventId, Long studentId);

    // === Thống kê / liệt kê ===
    long countByEventId(Long eventId);
    List<EntityQrTicket> findAllByEventId(Long eventId);
    List<EntityQrTicket> findAllByStudentId(Long studentId);

    // === Redeem 1 lần (đổi trạng thái ACTIVE -> REDEEMED nếu còn hiệu lực) ===
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE EntityQrTicket t
           SET t.status = tmtd.event.qr.EntityQrTicket.TicketStatus.REDEEMED,
               t.usedAt = :usedAt,
               t.usedBy = :usedBy
         WHERE t.token = :token
           AND t.eventId = :eventId
           AND t.status = tmtd.event.qr.EntityQrTicket.TicketStatus.ACTIVE
           AND (t.expiresAt IS NULL OR t.expiresAt > :now)
    """)
    int redeemOnce(@Param("token") String token,
                   @Param("eventId") Long eventId,
                   @Param("usedBy") Long usedBy,
                   @Param("usedAt") Instant usedAt,
                   @Param("now") Instant now);
}
