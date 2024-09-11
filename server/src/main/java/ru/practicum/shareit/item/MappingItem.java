package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


public class MappingItem {

    public static ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .comments(mapToCommentDto(item.getComments()))
                .build();
    }

    public static ItemDto mapToItemDto(Item item, List<Booking> bookings) {
        Booking lastBooking = bookings.getFirst();
        Booking nextBooking = bookings.getLast();
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(bookings.getFirst())
                .nextBooking(bookings.getLast())
                .comments(mapToCommentDto(item.getComments()))
                .build();
        if (lastBooking.equals(nextBooking)) {
            itemDto.setNextBooking(null);
        }
        return itemDto;
    }

    public static Item mapToItem(ItemDto itemDto, Long userId) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwnerId(userId);
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        return item;
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthorName())
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentDto> mapToCommentDto(Collection<Comment> comment) {
        return comment.stream().map(MappingItem::mapToCommentDto).toList();
    }

    public static Comment mapToComment(CommentDto commentDto, User user, Item item) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthorName(user.getName());
        comment.setCreated(LocalDateTime.now());
        comment.setItemId(item.getId());
        return comment;
    }

    public static ItemForRequestDto mapToItemForRequestDto(Item item) {
        return ItemForRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .build();
    }
}
