package tmtd.event.attendance.dto;

import java.time.Instant;

/** Trả về thông tin điểm danh */
public record AttendanceResponse(
    Long attendanceId,
    Long eventId,
    Integer studentId,
    boolean attended,
    Instant markedOn,
    String method
) {}
