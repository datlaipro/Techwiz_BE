package tmtd.event.qr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;  

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.Map; 
import java.awt.image.BufferedImage;          // ✅ THÊM
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import tmtd.event.qr.EntityQrTicket.TicketStatus;
import tmtd.event.qr.dto.QrRedeemResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class QrServiceImpl implements QrService {

    private final JpaQrTicket jpaQrTicket;

    // PNG 1x1 trong suốt (Base64) – ảnh placeholder hợp lệ cho endpoint
    // produces=IMAGE_PNG
    private static byte[] png1x1() {
        String b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAACklEQVR4nGNgAAAABgAB5SfUogAAAABJRU5ErkJggg==";
        return Base64.getDecoder().decode(b64);
    }

    // ✅ CHỮ KÝ KHỚP INTERFACE: (Long, Long, Integer)
    @Override
    public byte[] issueTicketPng(Long eventId, Long studentId, Integer size) {
        // 1) tạo token & lưu ticket như bạn đang làm
        String token = UUID.randomUUID().toString().replace("-", "");

        var ticket = new EntityQrTicket();
        ticket.setEventId(eventId);
        ticket.setStudentId(Math.toIntExact(studentId));
        ticket.setToken(token);
        ticket.setStatus(TicketStatus.ACTIVE);
        jpaQrTicket.save(ticket);

        // 2) render ảnh QR từ chính token (hoặc payload khác)
        int px = (size == null || size < 128) ? 256 : size;
        return renderQR(token, px);
    }

    private byte[] renderQR(String content, int size) {
        try {
            var hints = Map.of(
                    EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
                    EncodeHintType.MARGIN, 1 // viền nhỏ cho QR gọn
            );
            var writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to render QR", e);
        }
    }

    // ✅ CHỮ KÝ KHỚP INTERFACE (giữ nguyên nếu interface của bạn là redeem(String,
    // Long))
    @Override
    public QrRedeemResponse redeem(String token, Long eventId) {
        var ticket = jpaQrTicket.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QR token"));

        if (!ticket.getEventId().equals(eventId)) {
            return new QrRedeemResponse(false, "Event mismatch", null, null);
        }
        if (ticket.getStatus() == TicketStatus.REDEEMED) {
            return new QrRedeemResponse(false, "Already redeemed",
                    ticket.getEventId(), ticket.getStudentId());
        }

        ticket.setStatus(TicketStatus.REDEEMED); // entity dùng REDEEMED
        ticket.setUsedAt(Instant.now()); // dùng usedAt thay vì redeemedAt
        jpaQrTicket.save(ticket);

        return new QrRedeemResponse(true, "OK",
                ticket.getEventId(), ticket.getStudentId());
    }
}
