package ru.practicum.shareit.booking.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.MappingBooking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;

public class MappingBookingTest {

    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
    private Booking booking1;
    private Booking booking2;
    private List<Booking> bookings;
    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item(1L,"Mike","test1",1L,true,null, List.of());
        booking1 = new Booking(1L, item, 2L, start, end, 0);
        booking2 = new Booking(2L, item, 2L, start.plusDays(1), end.plusDays(3), 1);
        bookings = List.of(booking1, booking2);
    }

    @Test
    void mapBookingResponseDtoTestOne() {
        BookingResponseDto bookingResponseDto = MappingBooking.mapToBookingResponseDto(booking1);

        assertThat(booking1.getId(), equalTo(bookingResponseDto.getId()));
        assertThat(booking1.getStartTime(), equalTo(bookingResponseDto.getStart()));
        assertThat(booking1.getEndTime(), equalTo(bookingResponseDto.getEnd()));
        assertThat(booking1.getBookerId(), equalTo(bookingResponseDto.getBooker().getId()));
        assertThat(booking1.getStatus(), equalTo(bookingResponseDto.getStatus().ordinal()));
        assertThat(booking1.getItem().getId(), equalTo(bookingResponseDto.getItem().getId()));
    }

    @Test
    void mapBookingResponseDtoTestTwo() {
        booking1.setItem(null);
        BookingResponseDto bookingResponseDto = MappingBooking.mapToBookingResponseDto(booking1, item);

        assertThat(booking1.getId(), equalTo(bookingResponseDto.getId()));
        assertThat(booking1.getStartTime(), equalTo(bookingResponseDto.getStart()));
        assertThat(booking1.getEndTime(), equalTo(bookingResponseDto.getEnd()));
        assertThat(booking1.getBookerId(), equalTo(bookingResponseDto.getBooker().getId()));
        assertThat(booking1.getStatus(), equalTo(bookingResponseDto.getStatus().ordinal()));
        assertThat(item.getId(), equalTo(bookingResponseDto.getItem().getId()));
    }

    @Test
    void mapListBookingResponseDtoTest() {
        Collection<BookingResponseDto> bookingResponseDto = MappingBooking.mapToBookingResponseDto(bookings);
        Collection<BookingResponseDto> bookingResponseDebug = MappingBooking.mapToBookingResponseDto(bookings);

        bookingResponseDto.forEach(
                booking -> assertThat(bookingResponseDebug, hasItem(allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("start", equalTo(booking.getStart())),
                        hasProperty("end", equalTo(booking.getEnd())),
                        hasProperty("status", equalTo(booking.getStatus())),
                        hasProperty("item", allOf(
                                hasProperty("id", equalTo(booking.getItem().getId())))),
                        hasProperty("booker", allOf(
                                hasProperty("id", equalTo(booking.getBooker().getId())))
                        )))
                )
        );
    }

    @Test
    void mapToBookingTest() {
        BookingRequestDto requestDto = new BookingRequestDto(1L, start, end, BookingStatus.WAITING);
        Booking booking3 = MappingBooking.mapToBooking(2L, requestDto, item);

        bookingTest(booking1, booking3);
    }

    @Test
    void mapToBookingStateCurrentTest() {
        booking1.setStartTime(LocalDateTime.now().minusDays(1));
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "CURRENT");

        assertThat(bookingResponse, hasSize(1));
        bookingResponse.forEach(booking -> bookingTest(booking, booking1));
    }

    @Test
    void mapToBookingStatePastTest() {
        booking1.setStartTime(LocalDateTime.now().minusDays(5));
        booking1.setEndTime(LocalDateTime.now().minusDays(3));
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "PAST");

        assertThat(bookingResponse, hasSize(1));
        bookingResponse.forEach(booking -> bookingTest(booking, booking1));
    }

    @Test
    void mapToBookingStateFutureTest() {
        booking1.setStartTime(LocalDateTime.now().minusDays(1));
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "FUTURE");

        assertThat(bookingResponse, hasSize(1));
        bookingResponse.forEach(booking -> bookingTest(booking, booking2));
    }

    @Test
    void mapToBookingStateWaitingTest() {
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "WAITING");

        assertThat(bookingResponse, hasSize(1));
        bookingResponse.forEach(booking -> bookingTest(booking, booking1));
    }

    @Test
    void mapToBookingStateRejectedTest() {
        booking2.setStatus(2);
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "REJECTED");

        assertThat(bookingResponse, hasSize(1));
        bookingResponse.forEach(booking -> bookingTest(booking, booking2));
    }

    @Test
    void mapToBookingStateAllTest() {
        Collection<Booking> bookingResponse = MappingBooking.mapToBookingState(bookings, "ALL");

        assertThat(bookingResponse, hasSize(2));

        bookingResponse.forEach(
                booking -> assertThat(bookings, hasItem(allOf(
                        hasProperty("id", equalTo(booking.getId())),
                        hasProperty("startTime", equalTo(booking.getStartTime())),
                        hasProperty("endTime", equalTo(booking.getEndTime())),
                        hasProperty("status", equalTo(booking.getStatus())),
                        hasProperty("item", allOf(
                            hasProperty("id", equalTo(booking.getItem().getId())))),
                        hasProperty("bookerId", equalTo(booking.getBookerId()))
                        ))
                )
        );
    }

    private void bookingTest(Booking booking, Booking test) {
        assertThat(booking.getStartTime(), equalTo(test.getStartTime()));
        assertThat(booking.getEndTime(), equalTo(test.getEndTime()));
        assertThat(booking.getBookerId(), equalTo(test.getBookerId()));
        assertThat(booking.getStatus(), equalTo(test.getStatus()));
        assertThat(booking.getItem().getId(), equalTo(test.getItem().getId()));
    }
}
