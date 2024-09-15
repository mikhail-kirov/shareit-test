package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @Valid @RequestBody ItemDto item) {
        log.info("Запрос на добавление вещи {}", item.getName());
        return itemClient.addItem(userId, item);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                @PathVariable long itemId,
                                                @Valid @RequestBody CommentDto comment) {
        log.info("Запрос на добавление комментария к вещи с ID {}", itemId);
        return itemClient.setCommentInItem(bookerId, itemId, comment);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId,
                                             @RequestBody ItemDto item) {
        log.info("Запрос на обновление {} у пользователя с ID {}", item.getName(), userId);
        return itemClient.updateItemDto(userId, itemId, item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable long itemId) {
        log.info("Запрос на получение данных вещи с ID {} у пользователя с ID {}", itemId, userId);
        return itemClient.getItemDtoById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на получение списка данных по всем вещам пользователя с ID {}", userId);
        return itemClient.getItemsDtoByUserId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsBySearch(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam(value = "text") String text) {
        log.info("Запрос на поиск вещей пользователя с ID {} по параметру '{}'", userId, text);
        return itemClient.getItemsDtoBySearch(userId, text.toLowerCase());
    }
}
