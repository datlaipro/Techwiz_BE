package tmtd.event.events;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tmtd.event.events.dto.EventResponse;

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
    @GetMapping("/category/{category}")
public List<EventResponse> listApprovedByCategory(@PathVariable String category) {
    return service.listApprovedByCategory(category);
}

    
}
