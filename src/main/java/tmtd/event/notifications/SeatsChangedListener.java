// package tmtd.event.notifications;

// import lombok.RequiredArgsConstructor;
// import org.springframework.context.event.EventListener;
// import org.springframework.stereotype.Component;
// import tmtd.event.events.dto.SeatsUpdate;
// import tmtd.event.registrations.events.RegistrationSucceeded;
// // Nếu có event Cancelled, import thêm

// @Component
// @RequiredArgsConstructor
// public class SeatsChangedListener {
//   private final SeatsBroadcaster broadcaster;

//   @EventListener
//   public void onRegistrationSucceeded(RegistrationSucceeded e) {
//     // e nên có eventId, totalSeats, seatsAvailable (bạn có thể cho ServiceEvents trả về để build payload)
//     SeatsUpdate payload = new SeatsUpdate(
//         e.eventId(), e.totalSeats(), e.seatsAvailable(), System.currentTimeMillis()
//     );
//     broadcaster.broadcastSeats(e.eventId(), payload);
//   }

//   // @EventListener
//   // public void onRegistrationCancelled(RegistrationCancelled e) { ... }
// }


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
    // Lấy lại sự kiện từ DB để có totalSeats và seatsAvailable mới nhất
    var opt = events.findById(e.eventId());
    if (opt.isEmpty()) return;

    var ev = opt.get();
    int total = ev.getTotalSeats() == null ? 0 : ev.getTotalSeats();
    int available = ev.getSeatsAvailable() == null ? 0 : ev.getSeatsAvailable();

    SeatsUpdate payload = new SeatsUpdate(
        e.eventId(),
        total,
        available,
        System.currentTimeMillis()
    );

    broadcaster.broadcastSeats(e.eventId(), payload);
  }

  // Nếu có RegistrationCancelled:
  // @EventListener
  // public void onRegistrationCancelled(RegistrationCancelled e) {
  //   var opt = events.findById(e.eventId());
  //   if (opt.isEmpty()) return;
  //   var ev = opt.get();
  //   int total = ev.getTotalSeats() == null ? 0 : ev.getTotalSeats();
  //   int available = ev.getSeatsAvailable() == null ? 0 : ev.getSeatsAvailable();
  //   SeatsUpdate payload = new SeatsUpdate(e.eventId(), total, available, System.currentTimeMillis());
  //   broadcaster.broadcastSeats(e.eventId(), payload);
  // }
}

