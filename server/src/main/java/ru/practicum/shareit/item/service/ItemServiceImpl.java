package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.data.BookingRepository;
import ru.practicum.shareit.booking.data.CommentRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.data.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.item.ValidationItem;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ValidationUser validationUser;
    private final ValidationItem validationItem;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        validationUser.validationUserById(userId);
        Item item = itemRepository.save(MappingItem.mapToItem(itemDto, userId));
        log.info("{} добавлен(а)", item.getName());
        return MappingItem.mapToItemDto(item);
    }

    @Override
    public CommentDto setCommentInItem(Long bookerId, Long itemId, CommentDto commentDto) {
        User user = validationUser.validationUserById(bookerId);
        Item item = validationItem.validationItemById(itemId);
        List<Booking> bookings = bookingRepository.findAllBookingByBookerIdAndItemOrderByStartTimeDesc(bookerId, item);
        Booking booking = validationItem.validationCommentByBookerId(bookings);
        Comment comment = commentRepository.save(MappingItem.mapToComment(commentDto, user, booking.getItem()));
        return MappingItem.mapToCommentDto(comment);
    }

    @Override
    public ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto) {
        validationUser.validationUserById(userId);
        Item item = validationItem.validationItemByUser(itemId, userId);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info("Данные {} обновлены", item.getName());
        return MappingItem.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemDtoById(Long userId, Long itemId) {
        validationUser.validationUserById(userId);
        Item item = validationItem.validationItemById(itemId);
        return mappingItemToItemDto(item, userId);
    }

    @Override
    public Collection<ItemDto> getItemsDtoByUserId(Long userId) {
        validationUser.validationUserById(userId);
        Collection<Item> items = itemRepository.findByOwnerId(userId);
        log.info("Список всех вещей пользователя получен и отправлен");
        return items.stream().map(item -> mappingItemToItemDto(item, userId)).collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> getItemsDtoBySearch(Long userId, String search) {
        validationUser.validationUserById(userId);
        Collection<Item> items = itemRepository.getItemBySearch(search);
        log.info("Список для поиска получен");
        if (!search.trim().isEmpty()) {
            return items.stream()
                    .map(MappingItem::mapToItemDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private ItemDto mappingItemToItemDto(Item item, long userId) {
        List<Booking> bookings = getLastAndNextBookings(userId, item.getId());
        log.info("По ID {} найден(а) {}", item.getId(), item.getName());
        if (bookings.isEmpty()) {
            return MappingItem.mapToItemDto(item);
        }
        return MappingItem.mapToItemDto(item, bookings);
    }

    private List<Booking> getLastAndNextBookings(Long userId, Long itemId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllBookingByItemIdOrderByStartTimeAsc(itemId);
        if (!bookings.isEmpty()) {
            List<Booking> lastBooking = bookings.stream()
                    .filter(booking -> booking.getItem().getOwnerId().equals(userId))
                    .filter(booking -> booking.getStartTime().isBefore(currentDateTime))
                    .filter(booking -> !booking.getStatus().equals(2))
                    .reduce((first, second) -> second)
                    .stream().toList();
            List<Booking> nextBookings = bookings.stream()
                    .filter(booking -> booking.getItem().getOwnerId().equals(userId))
                    .filter(booking -> booking.getStartTime().isAfter(currentDateTime))
                    .filter(booking -> !booking.getStatus().equals(2))
                    .findFirst()
                    .stream().toList();
            return Stream.concat(lastBooking.stream(), nextBookings.stream()).collect(Collectors.toList());
        }
        return bookings;
    }
}
