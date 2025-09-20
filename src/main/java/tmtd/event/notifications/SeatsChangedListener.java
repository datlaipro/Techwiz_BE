package tmtd.event.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tmtd.event.events.JpaEvents;
import tmtd.event.events.dto.SeatsUpdate;
import tmtd.event.registrations.events.RegistrationSucceeded;

@Component
@RequiredArgsConstructor
public class SeatsChangedListener {

  private final SeatsBroadcaster broadcaster;
  private final JpaEvents events;

  @EventListener
  public void onRegistrationSucceeded(RegistrationSucceeded e) {
    // Lấy lại sự kiện từ DB để có số ghế còn lại mới nhất
    var opt = events.findById(e.eventId());
    if (opt.isEmpty()) return;

    var ev = opt.get();
    // Hiện tại totalSeats chính là SỐ GHẾ CÒN LẠI
    Long remaining = ev.getTotalSeats() == null ? 0L : ev.getTotalSeats();

    SeatsUpdate payload = new SeatsUpdate(
        e.eventId(),
        remaining,   // totalSeats (giữ nguyên vị trí tham số)
        remaining,   // seatsAvailable (nhưng giờ bằng remaining)
        System.currentTimeMillis()
    );

    broadcaster.broadcastSeats(e.eventId(), payload);
  }

  // Nếu có RegistrationCancelled, xử lý tương tự:
  // @EventListener
  // public void onRegistrationCancelled(RegistrationCancelled e) {
  //   var opt = events.findById(e.eventId());
  //   if (opt.isEmpty()) return;
  //   var ev = opt.get();
  //   Long remaining = ev.getTotalSeats() == null ? 0L : ev.getTotalSeats();
  //   SeatsUpdate payload = new SeatsUpdate(
  //       e.eventId(),
  //       remaining,
  //       remaining,
  //       System.currentTimeMillis()
  //   );
  //   broadcaster.broadcastSeats(e.eventId(), payload);
  // }
}
