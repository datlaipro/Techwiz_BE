package tmtd.event.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tmtd.event.events.dto.SeatsUpdate;

@Component
@RequiredArgsConstructor
public class SeatsBroadcaster {
  private final SimpMessagingTemplate simp;

  public void broadcastSeats(Long eventId, SeatsUpdate payload) {
    String dest = "/topic/events/" + eventId + "/seats";
    simp.convertAndSend(dest, payload);
  }
}
