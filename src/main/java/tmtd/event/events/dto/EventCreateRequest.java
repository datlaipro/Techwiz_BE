package tmtd.event.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventCreateRequest(
                @NotBlank String title,
                String description,
                String category,
                @NotNull LocalDate date,
                @NotNull LocalTime time,
                // ⬇️ Thêm cho sự kiện nhiều ngày (tùy chọn)
                LocalDate startDate,
                LocalDate endDate,
                String venue,
                Integer totalSeats,
                String mainImageUrl // <-- thêm
) {
}
