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

    // [ADD] Tìm vé theo token (để check-in nhanh)
    Optional<EntityQrTicket> findByToken(String token);

    // [ADD] Tìm vé theo event và student
    Optional<EntityQrTicket> findByEventIdAndStudentId(Long eventId, Integer studentId);

    // [ADD] Kiểm tra vé tồn tại
    boolean existsByEventIdAndStudentId(Long eventId, Integer studentId);

    // [ADD] Đổi trạng thái vé sang REDEEMED nếu hợp lệ (redeem 1 lần)
    @Transactional
    @Modifying
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

    // [ADD] Đếm tổng vé của một sự kiện
    long countByEventId(Long eventId);

    // [ADD] Lấy tất cả vé theo sự kiện
    List<EntityQrTicket> findAllByEventId(Long eventId);

    // [ADD] Lấy tất cả vé theo học viên
    List<EntityQrTicket> findAllByStudentId(Integer studentId);
}
