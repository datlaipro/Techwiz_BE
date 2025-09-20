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
        return renderQR(token, px);
    }

    @Override
    public byte[] issueTicketPng(Long eventId, Long studentId, int size) {
        int px = clampSize(size);

        // 1) Idempotent: nếu đã có vé ACTIVE và còn hạn -> dùng lại
        Optional<EntityQrTicket> existed = findActive(eventId, studentId);
        if (existed.isPresent()) {
            return renderQR(existed.get().getToken(), px);
        }

        // 2) Tạo mới (có thể gặp race-condition unique key)
        String token = UUID.randomUUID().toString().replace("-", "");
        EntityQrTicket ticket = new EntityQrTicket();
        ticket.setEventId(eventId);
        ticket.setStudentId(studentId);
        ticket.setToken(token);
        ticket.setStatus(TicketStatus.ACTIVE);
        // KHÔNG gọi setIssuedOn(...) vì entity không có setter này

        try {
            jpaQrTicket.save(ticket);
            return renderQR(token, px);

        } catch (DataIntegrityViolationException dup) {
            // 3) Nếu trùng unique (đã có vé được tạo song song) -> lấy lại vé ACTIVE để render
            return findActive(eventId, studentId)
                .map(t -> renderQR(t.getToken(), px))
                .orElseThrow(() -> dup);
        }
    }

    @Override
    public QrRedeemResponse redeem(String token, Long eventId) {
        var ticket = jpaQrTicket.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid QR token"));

        if (!Objects.equals(ticket.getEventId(), eventId)) {
            return new QrRedeemResponse(false, "Event mismatch", null, null);
        }
        if (ticket.getStatus() == TicketStatus.REDEEMED) {
            return new QrRedeemResponse(false, "Already redeemed",
                    ticket.getEventId(), ticket.getStudentId());
        }
        if (!notExpired(ticket)) {
            return new QrRedeemResponse(false, "QR expired",
                    ticket.getEventId(), ticket.getStudentId());
        }

        ticket.setStatus(TicketStatus.REDEEMED);
        ticket.setUsedAt(Instant.now());
        jpaQrTicket.save(ticket);

        return new QrRedeemResponse(true, "OK",
                ticket.getEventId(), ticket.getStudentId());
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
