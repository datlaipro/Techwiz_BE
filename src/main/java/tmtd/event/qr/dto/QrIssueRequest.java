package tmtd.event.qr.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * [ADD] DTO nhận yêu cầu phát vé QR
 * - eventId: ID sự kiện
 * - studentId: ID học viên
 * - size: Kích thước ảnh QR (px), có thể null -> mặc định 256px
 */
public record QrIssueRequest(
        @NotNull @Min(1) Long eventId,      // [ADD] Bắt buộc, ID sự kiện
        @NotNull @Min(1) Long studentId,    // [ADD] Bắt buộc, ID học viên
        Integer size                        // [ADD] Tùy chọn: kích thước QR code
) {}
