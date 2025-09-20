package tmtd.event.qr;

import java.util.Optional;
import tmtd.event.qr.dto.QrRedeemResponse;

public interface QrService {

  // Phát vé QR: trả về ảnh PNG của QR (payload là token)
  byte[] issueTicketPng(Long eventId, Long studentId, int size);

  // nếu đã có qr rồi thì render lại PNG từ vé đã có
  byte[] pngFromToken(String token, int size);

  // tìm vé ACTIVE (và còn hạn nếu có expiresAt)
  Optional<EntityQrTicket> findActive(Long eventId, Long studentId);

  // Quét vé QR (redeem): đổi trạng thái vé và đánh dấu điểm danh
  QrRedeemResponse redeem(String token, Long eventId);
}
