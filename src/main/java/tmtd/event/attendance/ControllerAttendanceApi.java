package tmtd.event.attendance;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmtd.event.attendance.dto.AttendanceMarkRequest;
import tmtd.event.attendance.dto.AttendanceResponse;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class ControllerAttendanceApi {

  private final ServiceAttendance service;

  @PostMapping("/mark")
  public ResponseEntity<AttendanceResponse> mark(@Valid @RequestBody AttendanceMarkRequest req){
    return ResponseEntity.ok(service.markOnce(req));
  }

  @GetMapping("/by-event/{eventId}")
  public ResponseEntity<List<AttendanceResponse>> byEvent(@PathVariable Long eventId){
    return ResponseEntity.ok(service.listByEvent(eventId));
  }

  @GetMapping("/stats/{eventId}")
  public ResponseEntity<Long> stats(@PathVariable Long eventId){
    return ResponseEntity.ok(service.statsAttended(eventId));
  }
}
