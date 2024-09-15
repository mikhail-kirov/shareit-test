package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.Booker;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private Booker booker;
    private ItemDto item;
}
