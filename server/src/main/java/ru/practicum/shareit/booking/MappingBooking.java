package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.MappingBooker;
import ru.practicum.shareit.validation.exeption.BadRequestException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MappingBooking {

    public static BookingResponseDto mapToBookingResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .status(BookingStatus.values()[booking.getStatus()])
                .booker(MappingBooker.mapToBooker(booking.getBookerId()))
                .item(MappingItem.mapToItemDto(booking.getItem()))
                .build();
    }

    public static BookingResponseDto mapToBookingResponseDto(Booking booking, Item item) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .status(BookingStatus.values()[booking.getStatus()])
                .booker(MappingBooker.mapToBooker(booking.getBookerId()))
                .item(MappingItem.mapToItemDto(item))
                .build();
    }

    public static Collection<BookingResponseDto> mapToBookingResponseDto(Collection<Booking> booking) {
        return booking.stream()
                .map(MappingBooking::mapToBookingResponseDto)
                .collect(Collectors.toList());
    }

    public static Booking mapToBooking(Long userId, BookingRequestDto bookingRequestDto, Item item) {
        return Booking.builder()
                .item(item)
                .bookerId(userId)
                .startTime(bookingRequestDto.getStart())
                .endTime(bookingRequestDto.getEnd())
                .status(bookingRequestDto.getStatus().ordinal())
                .build();
    }

    public static Collection<Booking> mapToBookingState(List<Booking> bookings, String state) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        RequestState requestState;
        try {
            requestState = RequestState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
        return switch (requestState) {
            case RequestState.CURRENT -> bookings.stream()
                    .filter(booking -> booking.getStartTime().isBefore(currentDateTime) &&
                            booking.getEndTime().isAfter(currentDateTime))
                    .toList();
            case RequestState.PAST -> bookings.stream()
                    .filter(booking -> booking.getEndTime().isBefore(currentDateTime))
                    .toList();
            case RequestState.FUTURE -> bookings.stream()
                    .filter(booking -> booking.getStartTime().isAfter(currentDateTime))
                    .toList();
            case RequestState.WAITING -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(0))
                    .toList();
            case RequestState.REJECTED -> bookings.stream()
                    .filter(booking -> booking.getStatus().equals(2))
                    .toList();
            case RequestState.ALL -> bookings;
        };
    }
}
