package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exeption.BadRequestException;

import java.util.Map;

@Service
@Slf4j
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addBooking(long userId, BookItemRequestDto requestDto) {
        validationBookingDto(requestDto);
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getAllBookingsByItemsByUser(long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("/owner?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> findBookingById(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approvedBooking(long userId, long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved.toString());
        String path = "/" + bookingId + "?approved={approved}";
        return patch(path, userId, parameters, null);
    }

    public ResponseEntity<Object> getAllBookingsByUser(long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("", userId, parameters);
    }

    private void validationBookingDto(BookItemRequestDto bookingRequestDto) {
        if (bookingRequestDto.getItemId() == null ||
            bookingRequestDto.getStart() == null ||
            bookingRequestDto.getEnd() == null) {
            log.info("Переданы некорректные или не полные данные для бронирования");
            throw new BadRequestException("Переданы некорректные или не полные данные для бронирования");
        }
    }
}
