package kyonggi.bookslyserver.domain.event.controller;

import jakarta.validation.constraints.NotNull;
import kyonggi.bookslyserver.domain.event.dto.request.CreateClosingEventRequestDto;
import kyonggi.bookslyserver.domain.event.dto.request.CreateTimeEventsRequestDto;
import kyonggi.bookslyserver.domain.event.dto.response.CreateClosingEventResponseDto;
import kyonggi.bookslyserver.domain.event.dto.response.CreateTimeEventsResponseDto;
import kyonggi.bookslyserver.domain.event.dto.response.GetClosingEventsResponseDto;
import kyonggi.bookslyserver.domain.event.dto.response.GetTimeEventsResponseDto;
import kyonggi.bookslyserver.domain.event.service.ClosingEventCommandService;
import kyonggi.bookslyserver.domain.event.service.ClosingEventQueryService;
import kyonggi.bookslyserver.domain.event.service.TimeEventCommandService;
import kyonggi.bookslyserver.domain.event.service.TimeEventQueryService;
import kyonggi.bookslyserver.domain.shop.entity.Shop.Shop;
import kyonggi.bookslyserver.global.auth.principal.shopOwner.OwnerId;
import kyonggi.bookslyserver.global.common.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    private final TimeEventCommandService timeEventCommandService;
    private final TimeEventQueryService timeEventQueryService;
    private final ClosingEventCommandService closingEventCommandService;
    private final ClosingEventQueryService closingEventQueryService;

    @PostMapping("/time-events")
    public ResponseEntity<SuccessResponse<?>> createTimeEvents(@OwnerId Long ownerId, @RequestBody CreateTimeEventsRequestDto createTimeEventsRequestDto) {
        CreateTimeEventsResponseDto timeEventsResponseDto = timeEventCommandService.createTimeEvents(ownerId, createTimeEventsRequestDto);
        return SuccessResponse.ok(timeEventsResponseDto);
    }

    @GetMapping("time-events")
    public ResponseEntity<SuccessResponse<?>> getTimeEvents(@RequestParam(value = "date", required = false) LocalDate date, @RequestParam("shop") @NotNull Long shopId,
                                                            @RequestParam("employee") @NotNull Long employeeId, @OwnerId Long ownerId) {
        GetTimeEventsResponseDto getTimeEventsResponseDto = timeEventQueryService.getTimeEvents(shopId, employeeId, date, ownerId);
        return SuccessResponse.ok(getTimeEventsResponseDto);
    }


    @PostMapping("/closing-events")
    public ResponseEntity<SuccessResponse<?>> createClosingEvent(@RequestBody CreateClosingEventRequestDto createClosingEventRequestDto) {
        CreateClosingEventResponseDto createClosingEventResponseDto = closingEventCommandService.createClosingEvent(createClosingEventRequestDto);
        return SuccessResponse.created(createClosingEventResponseDto);
    }

    @GetMapping("/closing-events")
    public ResponseEntity<SuccessResponse<?>> getClosingEvents(@RequestParam("shop") Long shopId, @OwnerId Long ownerId) {
        GetClosingEventsResponseDto getClosingEventsResponseDto = closingEventQueryService.getClosingEvents(shopId, ownerId);
        return SuccessResponse.ok(getClosingEventsResponseDto);
    }
}
