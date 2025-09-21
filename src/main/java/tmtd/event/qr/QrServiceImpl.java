package tmtd.event.qr;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import tmtd.event.qr.EntityQrTicket.TicketStatus;
import tmtd.event.qr.dto.QrRedeemResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class QrServiceImpl implements QrService {

    private final JpaQrTicket jpaQrTicket;

    private static final int MIN_SIZE = 128;
    private static final int MAX_SIZE = 640;

    private int clampSize(int size) {
        int s = (size <= 0 ? 256 : size);
        return Math.max(MIN_SIZE, Math.min(s, MAX_SIZE));
    }

    private boolean notExpired(EntityQrTicket t) {
        Instant exp = t.getExpiresAt();
        return exp == null || exp.isAfter(Instant.now());
    }

    /* ================== QR payload helpers ================== */

    /** Dạng payload: T:<token>|E:<eventId>|S:<studentId> */
    private String buildQrPayload(String token, Long eventId, Long studentId) {
        return "T:" + token + "|E:" + eventId + "|S:" + studentId;
    }

    /** Thử parse payload mới. Nếu không đúng định dạng -> Optional.empty() */
    private Optional<QrPayload> parseQrPayload(String content) {
        try {
            if (content == null) return Optional.empty();
            if (!content.contains("|") || !content.startsWith("T:")) return Optional.empty();
            String[] parts = content.split("\\|");
            String t = null; Long e = null; Long s = null;
            for (String p : parts) {
                int idx = p.indexOf(':');
                if (idx <= 0) continue;
                String k = p.substring(0, idx).trim();
                String v = p.substring(idx + 1).trim();
                if ("T".equals(k)) t = v;
                else if ("E".equals(k)) e = Long.valueOf(v);
                else if ("S".equals(k)) s = Long.valueOf(v);
            }
            if (t == null) return Optional.empty();
            return Optional.of(new QrPayload(t, e, s));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private record QrPayload(String token, Long eventId, Long studentId) {}

    /* ---------------- QrService API ---------------- */

    @Override
    @Transactional(readOnly = true)
    public Optional<EntityQrTicket> findActive(Long eventId, Long studentId) {
        return jpaQrTicket
            .findFirstByEventIdAndStudentIdAndStatus(eventId, studentId, TicketStatus.ACTIVE)
            .filter(this::notExpired);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] pngFromToken(String token, int size) {
        int px = clampSize(size);
        return renderQR(token, px); // giữ tương thích ngược
    }

    @Override
    public byte[] issueTicketPng(Long eventId, Long studentId, int size) {
        int px = clampSize(size);

        // 1) Idempotent
        Optional<EntityQrTicket> existed = findActive(eventId, studentId);
        if (existed.isPresent()) {
            String payload = buildQrPayload(existed.get().getToken(), eventId, studentId);
            return renderQR(payload, px);
        }

        // 2) Tạo mới
        String token = UUID.randomUUID().toString().replace("-", "");
        EntityQrTicket ticket = new EntityQrTicket();
        ticket.setEventId(eventId);
        ticket.setStudentId(studentId);
        ticket.setToken(token);
        ticket.setStatus(TicketStatus.ACTIVE);

        try {
            jpaQrTicket.save(ticket);
            String payload = buildQrPayload(token, eventId, studentId);
            return renderQR(payload, px);
        } catch (DataIntegrityViolationException dup) {
            // 3) Race-condition unique -> lấy lại vé ACTIVE
            return findActive(eventId, studentId)
                .map(t -> renderQR(buildQrPayload(t.getToken(), eventId, studentId), px))
                .orElseThrow(() -> dup);
        }
    }

    @Override
    public QrRedeemResponse redeem(String tokenOrPayload, Long eventIdFromApi) {
        if (tokenOrPayload == null || tokenOrPayload.isBlank()) {
            return new QrRedeemResponse(false, "Empty QR token", null, null);
        }

        // Hỗ trợ cả 2 dạng: token thuần / payload mới
        String token = tokenOrPayload;
        Long eventIdFromQr = null;
        Long studentIdFromQr = null;

        var parsed = parseQrPayload(tokenOrPayload);
        if (parsed.isPresent()) {
            token = parsed.get().token();
            eventIdFromQr = parsed.get().eventId();
            studentIdFromQr = parsed.get().studentId();
            // Chỉ coi là mismatch nếu CẢ HAI phía đều cung cấp eventId và KHÔNG khớp
            if (eventIdFromQr != null && eventIdFromApi != null
                    && !Objects.equals(eventIdFromQr, eventIdFromApi)) {
                return new QrRedeemResponse(false, "Event mismatch",
                        eventIdFromQr, studentIdFromQr);
            }
        }

        // Lấy vé theo token để trả thông tin
        var ticketOpt = jpaQrTicket.findByToken(token);
        if (ticketOpt.isEmpty()) {
            return new QrRedeemResponse(false, "Invalid QR token", null, null);
        }
        var ticket = ticketOpt.get();

        // Nếu API có truyền eventId thì đối chiếu (tùy chọn)
        if (eventIdFromApi != null && !Objects.equals(ticket.getEventId(), eventIdFromApi)) {
            return new QrRedeemResponse(false, "Event mismatch",
                    ticket.getEventId(), ticket.getStudentId());
        }

        // Hết hạn?
        if (!notExpired(ticket)) {
            return new QrRedeemResponse(false, "QR expired",
                    ticket.getEventId(), ticket.getStudentId());
        }

        // Đã dùng?
        if (ticket.getStatus() == TicketStatus.REDEEMED) {
            return new QrRedeemResponse(false, "Already redeemed",
                    ticket.getEventId(), ticket.getStudentId());
        }

        // UPDATE ATOMIC: ACTIVE -> REDEEMED (tránh race/409)
        Instant now = Instant.now();
        Long usedBy = null; // nếu có auth thì gán currentUserId
        int updated = jpaQrTicket.redeemOnce(
                token,
                // repo đã nới lỏng điều kiện (:eventId IS NULL OR t.eventId = :eventId)
                eventIdFromApi != null ? eventIdFromApi : eventIdFromQr,
                usedBy,
                now,
                now
        );

        org.slf4j.LoggerFactory.getLogger(getClass()).info(
                "[QR] redeem token={}, eventIdApi={}, eventIdQr={}, updated={}",
                token, eventIdFromApi, eventIdFromQr, updated
        );

        if (updated == 1) {
            return new QrRedeemResponse(true, "OK",
                    ticket.getEventId(), ticket.getStudentId());
        }

        // Không update được: có thể vừa bị người khác redeem/expired/khác điều kiện
        var check = jpaQrTicket.findByToken(token).orElse(ticket);
        if (!notExpired(check)) {
            return new QrRedeemResponse(false, "QR expired",
                    check.getEventId(), check.getStudentId());
        }
        if (check.getStatus() == TicketStatus.REDEEMED) {
            return new QrRedeemResponse(false, "Already redeemed",
                    check.getEventId(), check.getStudentId());
        }

        // Trường hợp hiếm
        return new QrRedeemResponse(false, "Redeem rejected by conditions",
                check.getEventId(), check.getStudentId());
    }

    /* ---------------- Helpers ---------------- */

    private byte[] renderQR(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to render QR", e);
        }
    }
}
