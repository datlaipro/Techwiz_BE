package tmtd.event.qr;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    int size = (req.size() == null || req.size() < 128) ? 256 : req.size();
    byte[] png = qrService.issueTicketPng(req.eventId(), req.studentId(), size);

    // Log để chắc chắn có body > 0
    org.slf4j.LoggerFactory.getLogger(getClass())
        .info("[QR] issue event={} student={} -> pngBytes={}", req.eventId(), req.studentId(),
            (png == null ? "null" : png.length));

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG) // ✅ Content-Type
        .cacheControl(org.springframework.http.CacheControl.noCache())
        .header("Content-Disposition", "inline; filename=\"ticket.png\"")
        .body(png); // ✅ Body ảnh
  }

  @PostMapping("/redeem")
  public ResponseEntity<tmtd.event.qr.dto.QrRedeemResponse> redeem(
      @Valid @RequestBody tmtd.event.qr.dto.QrRedeemRequest req) {

    var res = qrService.redeem(req.token(), req.eventId());
    if (res.success()) {
      attendanceService.markOnce(
          new tmtd.event.attendance.dto.AttendanceMarkRequest(
              res.eventId(), res.studentId(), "QR"));
    }
    return res.success()
        ? ResponseEntity.ok(res)
        : ResponseEntity.status(409).body(res);
  }
}
