package tmtd.event.qr.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * [ADD] DTO nhận yêu cầu quét (redeem) vé QR
 * - token: Mã token trong QR code
 * - eventId: ID sự kiện để xác thực vé thuộc sự kiện nào
 */
public record QrRedeemRequest(
                @NotBlank String token, // [ADD] Bắt buộc: mã token QR
                @Min(1) Long eventId // [ADD] Bắt buộc: ID sự kiện
) {
}
