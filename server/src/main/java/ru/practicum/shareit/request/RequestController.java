package ru.practicum.shareit.request;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public ReqResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ReqRequestDto reqRequestDto) {
        return requestService.createRequest(userId, reqRequestDto);
    }

    @GetMapping("/{requestId}")
    public ReqResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {
        return requestService.getRequestById(requestId, userId);
    }

    @GetMapping
    public Collection<ReqResponseDto> getRequestsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public Collection<ReqResponseDto> getRequestsByOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return requestService.getRequestsByOtherUsers(new GetAllRequestDto(userId,from,size));
    }
}