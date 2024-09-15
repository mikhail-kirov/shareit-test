package ru.practicum.shareit.validation.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.data.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.item.ValidationItem;


@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationBooking {

    private final BookingRepository bookingRepository;
    private final ValidationItem validationItem;

    public Booking validationBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + id + " не найдено"));
    }

    public Item validationBookingDto(BookingRequestDto bookingRequestDto, Long userId) {
        Item item = validationItem.validationItemById(bookingRequestDto.getItemId());
        if (item.getOwnerId().equals(userId)) {
            log.info("ID собственника и арендатора совпадают");
            throw new NotFoundException("ID собственника и арендатора совпадают");
        }
        if (!item.getAvailable()) {
            log.info("Установлен запрет на бронирование вещь с ID {}", bookingRequestDto.getItemId());
            throw new BadRequestException("Бронирование вещи недоступно");
        }
        return item;
    }
}
