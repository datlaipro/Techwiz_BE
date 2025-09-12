package tmtd.event.attendance;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmtd.event.attendance.dto.AttendanceMarkRequest;
import tmtd.event.attendance.dto.AttendanceResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceAttendanceImpl implements ServiceAttendance {

  private final JpaAttendance jpa;

  private static AttendanceResponse toDto(EntityAttendance e){
    return new AttendanceResponse(
      e.getAttendanceId(),
      e.getEventId(),
      e.getStudentId(),
      e.isAttended(),
      e.getMarkedOn(),
      e.getMethod()
    );
  }

  @Override
  public AttendanceResponse markOnce(AttendanceMarkRequest req) {
    var method = (req.method()==null || req.method().isBlank())
        ? "QR" : req.method().trim().toUpperCase();

    // 1) thử insert nếu chưa có (fast path)
    int inserted = jpa.markAttendedOnce(req.eventId(), req.studentId(), method);
    if (inserted == 1) {
      // lấy lại để trả DTO
      return jpa.findByEventIdAndStudentId(req.eventId(), req.studentId())
                .map(ServiceAttendanceImpl::toDto)
                .orElseGet(() -> {
                  // fallback cực đoan (gần như không xảy ra)
                  var e = new EntityAttendance();
                  e.setEventId(req.eventId());
                  e.setStudentId(req.studentId());
                  e.setMethod(method);
                  e.setMarkedOn(Instant.now());
                  e.setAttended(true);
                  return toDto(jpa.save(e));
                });
    }

    // 2) nếu đã tồn tại: cập nhật thời điểm + method (tuỳ yêu cầu)
    var e = jpa.findByEventIdAndStudentId(req.eventId(), req.studentId())
               .orElseThrow(); // về lý thuyết phải có
    e.setMarkedOn(Instant.now());
    e.setMethod(method);
    e.setAttended(true);
    return toDto(jpa.save(e));
  }

  @Override
  @Transactional(readOnly = true)
  public List<AttendanceResponse> listByEvent(Long eventId) {
    return jpa.findByEventId(eventId).stream().map(ServiceAttendanceImpl::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long statsAttended(Long eventId) {
    return jpa.countDistinctAttendees(eventId);
  }
}
