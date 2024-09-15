package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;


@Service
@Slf4j
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long userId, ItemDto item) {
        validateItemDto(item);
        return post("", userId, item);
    }

    public ResponseEntity<Object> updateItemDto(long userId, long itemId, ItemDto item) {
        return patch("/" + itemId, userId, item);
    }

    public ResponseEntity<Object> getItemDtoById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsDtoByUserId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemsDtoBySearch(long userId, String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get("/search?text={text}", userId, parameters);
    }

    public ResponseEntity<Object> setCommentInItem(long bookerId, long itemId, CommentDto comment) {
        validateComment(comment);
        return post("/" + itemId + "/comment", bookerId, comment);
    }

    private void validateComment(CommentDto comment) {
        if (comment.getText() == null || comment.getText().isEmpty()) {
            log.info("Комментарий на получен");
            throw new BadRequestException("Комментарий на получен");
        }
    }

    private void validateItemDto(ItemDto item) {
        if (item.getName() == null ||
            item.getDescription() == null ||
            item.getAvailable() == null) {
            log.info("Некорректные данные вещи");
            throw new BadRequestException("Некорректные данные вещи");
        }
    }
}
