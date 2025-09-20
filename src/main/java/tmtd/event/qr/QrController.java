package tmtd.event.qr;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tmtd.event.attendance.ServiceAttendance;
import tmtd.event.attendance.dto.AttendanceMarkRequest;
import tmtd.event.qr.dto.QrIssueRequest;
import tmtd.event.qr.dto.QrRedeemRequest;
import tmtd.event.qr.dto.QrRedeemResponse;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

  private final QrService qrService;
  private final ServiceAttendance attendanceService;
  // Nếu bạn có AuthFacade, có thể inject vào và kiểm tra "studentId == currentUser":
  // private final AuthFacade auth;

  @PostMapping(
      value = "/issue",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.IMAGE_PNG_VALUE
  )
  public ResponseEntity<byte[]> issue(@Valid @RequestBody QrIssueRequest req) {
    var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    org.slf4j.LoggerFactory.getLogger(getClass()).info(
        "[QR] /issue principal={} authorities={}",
        (a == null ? "null" : a.getPrincipal()),
        (a == null ? "null" : a.getAuthorities())
    );

    // --- Validate body ---
    if (req == null || req.eventId() == null || req.studentId() == null) {
      throw new ResponseStatusException(BAD_REQUEST, "eventId/studentId is required");
    }

    // (Tùy chọn) Ràng buộc user: chỉ chủ sở hữu hoặc admin mới được lấy QR
    // boolean isAdmin = auth.hasRole(Roles.ADMIN);
    // Long current = auth.currentUserId();
    // if (!isAdmin && (current == null || !current.equals(req.studentId()))) {
    //   throw new ResponseStatusException(FORBIDDEN, "Not your QR");
    // }

    // --- Clamp size ---
    long sizeReq = req.size() == null ? 256L : req.size();
    int size = (int) Math.max(128, Math.min(sizeReq, 640)); // 128..640

    try {
      byte[] png = qrService.issueTicketPng(
          req.eventId(),
          req.studentId(),
          size
      );
      if (png == null || png.length == 0) {
        throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "QR generation returned empty");
      }

      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_PNG)
          .cacheControl(CacheControl.noCache())
          .header("Content-Disposition", "inline; filename=\"ticket.png\"")
          .body(png);

    } catch (java.util.NoSuchElementException | jakarta.persistence.EntityNotFoundException e) {
      // ví dụ: không tìm thấy registration/event
      throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
    } catch (IllegalStateException e) {
      // ví dụ: chưa CONFIRMED / đã hủy / hết ghế...
      throw new ResponseStatusException(CONFLICT, e.getMessage(), e);
    } catch (org.springframework.security.access.AccessDeniedException e) {
      throw new ResponseStatusException(FORBIDDEN, e.getMessage(), e);
    } catch (Exception e) {
      // Bắt mọi lỗi còn lại để không rơi 500 “mù”
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "QR issue failed", e);
    }
  }

  @PostMapping(
      value = "/redeem",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  // @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')") // bật lại nếu bạn dùng security theo role
  public ResponseEntity<QrRedeemResponse> redeem(@Valid @RequestBody QrRedeemRequest req) {
    try {
      var res = qrService.redeem(req.token(), req.eventId());
      if (!res.success()) {
        return ResponseEntity.status(CONFLICT).body(res);
      }
      attendanceService.markOnce(new AttendanceMarkRequest(
          res.eventId(),
          res.studentId(),
          "QR_GATE"
      ));
      return ResponseEntity.ok(res);

    } catch (java.util.NoSuchElementException | jakarta.persistence.EntityNotFoundException e) {
      throw new ResponseStatusException(NOT_FOUND, e.getMessage(), e);
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(CONFLICT, e.getMessage(), e);
    } catch (org.springframework.security.access.AccessDeniedException e) {
      throw new ResponseStatusException(FORBIDDEN, e.getMessage(), e);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "QR redeem failed", e);
    }
  }
}
