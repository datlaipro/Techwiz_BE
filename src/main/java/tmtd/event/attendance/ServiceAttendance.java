package tmtd.event.attendance;

import java.util.List;
import tmtd.event.attendance.dto.AttendanceMarkRequest;
import tmtd.event.attendance.dto.AttendanceResponse;

public interface ServiceAttendance {
  AttendanceResponse markOnce(AttendanceMarkRequest req); // upsert theo (eventId, studentId)
  List<AttendanceResponse> listByEvent(Long eventId);
  long statsAttended(Long eventId);
}
