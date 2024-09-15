package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItemDto(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemDtoById(Long userId, Long itemId);

    Collection<ItemDto> getItemsDtoByUserId(Long userId);

    Collection<ItemDto> getItemsDtoBySearch(Long userId, String search);

    CommentDto setCommentInItem(Long userId, Long itemId, CommentDto commentDto);
}
