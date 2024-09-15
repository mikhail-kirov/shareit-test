package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.Collection;

public interface BookingService {

    BookingResponseDto addBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approvedBooking(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto findBookingById(Long userId, Long bookingId);

    Collection<BookingResponseDto> getAllBookingsByUser(Long userId, String state);

    Collection<BookingResponseDto> getAllBookingsByItemsByUser(Long userId, String state);
}
