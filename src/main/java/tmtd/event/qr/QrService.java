package tmtd.event.qr;

import tmtd.event.qr.dto.QrRedeemResponse;

public interface QrService {

    // Phát vé QR: trả về ảnh PNG của QR (payload là token)
    byte[] issueTicketPng(Long eventId, Long studentId, Integer size); // size có thể null -> mặc định trong impl
  
    // Quét vé QR (redeem): đổi trạng thái vé và đánh dấu điểm danh
    QrRedeemResponse redeem(String token, Long eventId);
}
