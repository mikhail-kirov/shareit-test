package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
										        @RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Запрос на добавление брони для вещь с ID {}", requestDto.getItemId());
		return bookingClient.addBooking(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
											 @PathVariable Long bookingId) {
		log.info("Запрос на получение данных о брони с ID {}", bookingId);
		return bookingClient.findBookingById(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingsByItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
															  @RequestParam (value = "state", defaultValue = "ALL") String state) {
		log.info("Запрос на получение списка бронирований для всех вещей пользователя с ID {}", userId);
		return bookingClient.getAllBookingsByItemsByUser(userId, state);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBookingStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
												      @PathVariable Long bookingId,
												      @RequestParam (value = "approved") Boolean approved) {
		log.info("Запрос на обновление статуса брони с ID {}", bookingId);
		return bookingClient.approvedBooking(userId, bookingId, approved);
	}

	@GetMapping
	public ResponseEntity<Object> getAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
													   @RequestParam (value = "state", defaultValue = "ALL") String state) {
		log.info("Запрос на получение списка всех бронирований пользователя с ID {}", userId);
		return bookingClient.getAllBookingsByUser(userId, state);
	}
}
