package kyonggi.bookslyserver.domain.shop.dto.request.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record EmployeeCreateRequestDto(
        @NotNull String employeeName,
        @NotNull String description,
        @NotNull String imgUri,
        List<Long> menus,

        @NotNull List<EmployeeWorkScheduleDto> workSchedules
) {
}
