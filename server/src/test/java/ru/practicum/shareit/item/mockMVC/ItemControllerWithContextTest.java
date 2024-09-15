package ru.practicum.shareit.item.mockMVC;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.exeption.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerWithContextTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;

    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private CommentDto comment1;
    private CommentDto comment2;
    private List<CommentDto> comments;
    private List<ItemDto> items;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @BeforeEach
    void setUp() {
        itemDto1 = ItemDto.builder()
                .id(1L)
                .name("Mik")
                .description("test1")
                .available(true)
                .requestId(2L)
                .build();
        itemDto2 = ItemDto.builder()
                .id(2L)
                .name("Mark")
                .description("test2")
                .available(false)
                .requestId(9L)
                .build();

        items = List.of(itemDto1, itemDto2);

        comment1 = CommentDto.builder()
                .id(3L)
                .text("test1")
                .authorName("Mike")
                .created(date)
                .build();
        comment2 = CommentDto.builder()
                .id(5L)
                .text("test2")
                .authorName("Oleg")
                .created(date.plusDays(2))
                .build();

        comments = List.of(comment1, comment2);
    }

    @Test
    void createItem() throws Exception {
        ItemDto itemDtoTest = ItemDto.builder().name("Mik").description("test1").available(true).requestId(2L).build();
        when(itemService.addItem(anyLong(), any())).thenReturn(itemDto1);

        mockMvc.perform(post("/items")
                .content(mapper.writeValueAsString(itemDtoTest))
                .header("X-Sharer-User-Id", 1L)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is(itemDto1.getName())))
                    .andExpect(jsonPath("$.description", is(itemDto1.getDescription())))
                    .andExpect(jsonPath("$.available", is(itemDto1.getAvailable())))
                    .andExpect(jsonPath("$.requestId", is(itemDto1.getRequestId()), Long.class));
    }

    @Test
    void createComment() throws Exception {
        CommentDto comment = CommentDto.builder().text("test1").authorName("Mike").created(date).build();
        when(itemService.setCommentInItem(anyLong(), anyLong(), any())).thenReturn(comment1);

        mockMvc.perform(post("/items/1/comment")
                .content(mapper.writeValueAsString(comment))
                .header("X-Sharer-User-Id", 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.text", is(comment1.getText())))
                    .andExpect(jsonPath("$.authorName", is(comment1.getAuthorName())))
                    .andExpect(jsonPath("$.created", is(comment1.getCreated().toString())));
    }

    @Test
    void updateItem() throws Exception {
        ItemDto itemDtoTest = ItemDto.builder().id(1L).name("Arsen").description("test2").available(false).requestId(2L).build();
        when(itemService.updateItemDto(anyLong(), anyLong(), any())).thenReturn(itemDtoTest);

        mockMvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDtoTest))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(itemDtoTest.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoTest.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoTest.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoTest.getRequestId()), Long.class));
    }

    @Test
    void getItemById() throws Exception {
        itemDto1.setComments(comments);
        when(itemService.getItemDtoById(anyLong(), anyLong())).thenReturn(itemDto1);
        String[] created = {comment1.getCreated().toString(), comment2.getCreated().toString()};

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(itemDto1.getName())))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto1.getRequestId()), Long.class))
            .andExpect(jsonPath("$.comments[*].id", containsInAnyOrder(3, 5)))
            .andExpect(jsonPath("$.comments[*].text", containsInAnyOrder(comment1.getText(), comment2.getText())))
            .andExpect(jsonPath("$.comments[*].authorName", containsInAnyOrder(comment1.getAuthorName(), comment2.getAuthorName())))
            .andExpect(jsonPath("$.comments[*].created", containsInAnyOrder(created)));
    }

    @Test
    void getItemsByUser() throws Exception {
        when(itemService.getItemsDtoByUserId(anyLong())).thenReturn(items);
        mockMvcItemDtoListTest("/items");
    }

    @Test
    void getItemsBySearch() throws Exception {
        when(itemService.getItemsDtoBySearch(anyLong(), anyString())).thenReturn(items);
        mockMvcItemDtoListTest("/items/search?text=mik");
    }

    @Test
    void findItemNotFoundException() throws Exception {
        when(itemService.getItemDtoById(anyLong(), any())).thenThrow(new NotFoundException("Вещь с ID 1 не найдена"));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь с ID 1 не найдена")));
    }

    @Test
    void updateItemNotFoundException() throws Exception {
        when(itemService.updateItemDto(anyLong(), anyLong(), any())).thenThrow(new NotFoundException("Вещь не принадлежит пользователю с ID 2"));

        mockMvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto1))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь не принадлежит пользователю с ID 2")));
    }

    @Test
    void setCommentBadRequestExceptionTest() throws Exception {
        when(itemService.setCommentInItem(anyLong(), anyLong(), any()))
                .thenThrow(new BadRequestException("Пользоваетель не может оставить комментарий"));

        mockMvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment1))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Пользоваетель не может оставить комментарий")));
    }

    private void mockMvcItemDtoListTest(String url) throws Exception {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(itemDto1.getName(), itemDto2.getName())))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(itemDto1.getDescription(), itemDto2.getDescription())))
                .andExpect(jsonPath("$[*].available", containsInAnyOrder(itemDto1.getAvailable(), itemDto2.getAvailable())))
                .andExpect(jsonPath("$[*].requestId", containsInAnyOrder(2, 9)));
    }
}
