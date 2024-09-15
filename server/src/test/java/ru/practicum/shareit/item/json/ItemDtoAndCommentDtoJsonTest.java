package ru.practicum.shareit.item.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemDtoAndCommentDtoJsonTest {
    private final JacksonTester<ItemDto> jsonItem;
    private final JacksonTester<CommentDto> jsonComment;
    private final JacksonTester<ItemForRequestDto> jsonItemForRequest;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime createdDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Test
    void testItemDto() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(4L)
                .name("sdfgsdfg")
                .description("test1")
                .requestId(5L)
                .available(true)
                .comments(List.of())
                .build();

        JsonContent<ItemDto> result = jsonItem.write(item);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(4);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(item.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEqualTo(List.of());
    }

    @Test
    void testCommentDto() throws Exception {
        CommentDto comment = CommentDto.builder()
                .id(3L)
                .text("sdfgsdfg")
                .authorName("Mike")
                .created(createdDate)
                .build();

        JsonContent<CommentDto> result = jsonComment.write(comment);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(comment.getText());
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(comment.getAuthorName());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(comment.getCreated().toString());
    }

    @Test
    void testItemForRequestDto() throws Exception {
        ItemForRequestDto itemForRequest = ItemForRequestDto.builder()
                .id(7L)
                .ownerId(1L)
                .name("Mike")
                .build();

        JsonContent<ItemForRequestDto> result = jsonItemForRequest.write(itemForRequest);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(itemForRequest.getName());
    }
}
