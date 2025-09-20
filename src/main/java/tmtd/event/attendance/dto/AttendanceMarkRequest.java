package tmtd.event.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Yêu cầu đánh dấu điểm danh */
public record AttendanceMarkRequest(
    @NotNull @Positive Long eventId,
    @NotNull @Positive Long studentId,
    /** "QR" hoặc "MANUAL" (tùy chọn, mặc định QR) */
    String method
) {}
