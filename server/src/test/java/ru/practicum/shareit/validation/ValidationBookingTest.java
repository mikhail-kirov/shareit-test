package ru.practicum.shareit.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.data.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.validation.booking.ValidationBooking;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.item.ValidationItem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationBookingTest {
    @Mock
    private ValidationItem validItem;
    @Mock
    private BookingRepository bookingRepo;

    private ValidationBooking validationBooking;
    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
    private Item item;

    @BeforeEach
    public void setUp() {
        validationBooking = new ValidationBooking(
                bookingRepo,
                validItem
        );

        item = new Item(4L,"Mike","test1",1L,true,null, List.of());
    }

    @Test
    void testCreateBookingWithNotFoundException() {
        when(validItem.validationItemById(anyLong())).thenReturn(item);

        BookingRequestDto bookingRequest = new BookingRequestDto(1L, start, end, null);
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> validationBooking.validationBookingDto(bookingRequest, 1L)
        );
        assertEquals("ID собственника и арендатора совпадают", exception.getMessage());
    }

    @Test
    void testCreateBookingWithBadRequestException() {
        item.setAvailable(false);
        item.setOwnerId(3L);
        when(validItem.validationItemById(anyLong())).thenReturn(item);

        BookingRequestDto bookingRequest = new BookingRequestDto(1L, start, end, null);
        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> validationBooking.validationBookingDto(bookingRequest, 1L)
        );
        assertEquals("Бронирование вещи недоступно", exception.getMessage());
    }

    @Test
    void testValidateBookingByIdNotFoundException() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> validationBooking.validationBookingById(1L)
        );
        assertEquals("Бронирование с ID 1 не найдено", exception.getMessage());
    }
}
