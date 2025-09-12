package tmtd.event.qr;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tmtd.event.attendance.ServiceAttendance;
import tmtd.event.attendance.dto.AttendanceMarkRequest;
import tmtd.event.qr.dto.QrIssueRequest;
import tmtd.event.qr.dto.QrRedeemRequest;
import tmtd.event.qr.dto.QrRedeemResponse;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

  private final QrService qrService;
  private final ServiceAttendance attendanceService;

  @PostMapping(value = "/issue", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> issue(@Valid @RequestBody QrIssueRequest req) {
    var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    org.slf4j.LoggerFactory.getLogger(getClass()).info("[QR] /issue principal={} authorities={}",
        (a == null ? "null" : a.getPrincipal()), (a == null ? "null" : a.getAuthorities()));

    int size = (req.size() == null || req.size() < 128) ? 256 : req.size();
    byte[] png = qrService.issueTicketPng(req.eventId(), req.studentId(), size);
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .cacheControl(CacheControl.noCache())
        .header("Content-Disposition", "inline; filename=\"ticket.png\"")
        .body(png);
  }

  @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
  @PostMapping("/redeem")
  public ResponseEntity<QrRedeemResponse> redeem(@Valid @RequestBody QrRedeemRequest req) {
    var res = qrService.redeem(req.token(), req.eventId());
    if (!res.success()) {
      return ResponseEntity.status(409).body(res);
    }
    attendanceService.markOnce(
        new AttendanceMarkRequest(res.eventId(), res.studentId(), "QR_GATE"));
    return ResponseEntity.ok(res);
  }

}
