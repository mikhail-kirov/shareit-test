package ru.practicum.shareit.validation.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.data.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.exeption.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationItem {

    private final ItemRepository itemRepository;

    public Item validationItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + id + " не найдена"));
    }

    public Item validationItemByUser(Long itemId, Long userId) {
        Item item = validationItemById(itemId);
        if (!item.getOwnerId().equals(userId)) {
            log.info("{} не принадлежит пользователю с ID {}", item.getName(), userId);
            throw new NotFoundException(item.getName() + " не принадлежит пользователю с ID " + userId);
        }
        return item;
    }

    public Booking validationCommentByBookerId(List<Booking> bookings) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> !booking.getStartTime().isAfter(currentDateTime))
                .filter(booking -> !booking.getStatus().equals(2))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Пользоваетель не может оставить комментарий"));
    }
}
