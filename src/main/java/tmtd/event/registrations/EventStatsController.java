package tmtd.event.registrations;

import java.util.List;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import tmtd.event.registrations.dto.EventCountersDto;
import tmtd.event.registrations.repo.EventStatsRepo;

// tmtd/event/registrations/EventStatsController.java
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class EventStatsController {
    private final EventStatsRepo statsRepo;

    @GetMapping("/events-with-stats/{id}")
    public ResponseEntity<EventCountersDto> getOne(@PathVariable Long id) {
        return ResponseEntity.of(statsRepo.getStatsByEventId(id)); // repo trả Optional<EventCountersDto>
    }

    // THAY chỗ trả danh sách (nếu có) bằng:
    @GetMapping("/events-with-stats")
    public ResponseEntity<List<EventCountersDto>> listAll() {
        return ResponseEntity.ok(statsRepo.listApprovedWithStats());
    }
}
