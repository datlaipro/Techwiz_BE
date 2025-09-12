package tmtd.event.events;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tmtd.event.events.dto.EventResponse;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class PublicEventsController {

    private final EventsService service;

    @GetMapping
    public List<EventResponse> listApproved(){
        return service.listApproved();
    }

    @GetMapping("/{id}")
    public EventResponse getApproved(@PathVariable Long id){
        return service.getApprovedById(id);
    }
}
