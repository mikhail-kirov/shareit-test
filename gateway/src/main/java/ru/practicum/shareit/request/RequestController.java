package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ReqRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody ReqRequestDto reqRequestDto) {
        return requestClient.createRequest(userId, reqRequestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable long requestId) {
        return requestClient.getRequestById(requestId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestsByOtherUsers(@RequestHeader("X-Sharer-User-Id") long userId,
                                                          @RequestParam(name = "from", defaultValue = "0") int from,
                                                          @RequestParam(name = "size", defaultValue = "10") int size) {
        return requestClient.getRequestsByOtherUsers(userId, from, size);
    }
}
