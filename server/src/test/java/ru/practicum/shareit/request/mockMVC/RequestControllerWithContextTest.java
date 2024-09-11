package ru.practicum.shareit.request.mockMVC;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.RequestController;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.validation.exeption.NotFoundException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
public class RequestControllerWithContextTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RequestService requestService;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime createdDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private ReqResponseDto responseDto1;
    private ReqResponseDto responseDto2;
    List<ReqResponseDto> responseDtoList;

    @BeforeEach
    void setUp() {
        responseDto1 = ReqResponseDto.builder()
                .id(1L)
                .description("test")
                .created(createdDate)
                .build();
        responseDto2 = ReqResponseDto.builder()
                .id(2L)
                .description("test")
                .created(createdDate)
                .build();
        responseDtoList = List.of(responseDto1, responseDto2);
    }

    @Test
    void createRequest() throws Exception {
        ReqResponseDto reqResponse = ReqResponseDto.builder().description("test").build();
        when(requestService.createRequest(anyLong(), any())).thenReturn(responseDto1);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(reqResponse))
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is(responseDto1.getDescription())))
                .andExpect(jsonPath("$.created", is(responseDto1.getCreated().toString())));
    }

    @Test
    void getRequest() throws Exception {
        ItemForRequestDto itemForRequest1 = ItemForRequestDto.builder()
                .id(7L)
                .ownerId(1L)
                .name("Mike")
                .build();
        ItemForRequestDto itemForRequest2 = ItemForRequestDto.builder()
                .id(9L)
                .ownerId(11L)
                .name("Max")
                .build();
        responseDto1.setItems(List.of(itemForRequest1, itemForRequest2));

        when(requestService.getRequestById(anyLong(), anyLong())).thenReturn(responseDto1);

        mockMvc.perform(get("/requests/1")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.description", is(responseDto1.getDescription())))
            .andExpect(jsonPath("$.created", is(responseDto1.getCreated().toString())))
            .andExpect(jsonPath("$.items[*].id", containsInAnyOrder(7, 9)))
            .andExpect(jsonPath("$.items[*].ownerId", containsInAnyOrder(1, 11)))
            .andExpect(jsonPath("$.items[*].name", containsInAnyOrder(itemForRequest1.getName(), itemForRequest2.getName())));
    }

    @Test
    void getAllRequestsByUser() throws Exception {
        when(requestService.getRequestsByUserId(anyLong())).thenReturn(responseDtoList);
        reqResponseDtoListTest("/requests");
    }

    @Test
    void getRequestsByOtherUsers() throws Exception {
        when(requestService.getRequestsByOtherUsers(any())).thenReturn(responseDtoList);
        reqResponseDtoListTest("/requests/all");
    }

    @Test
    void testUserNotFoundException() throws Exception {
        when(requestService.getRequestsByUserId(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", anyLong())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 1 не зарегистрирован")));
    }

    @Test
    void testRequestNotFoundException() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Запрос с ID 1 не найден"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Запрос с ID 1 не найден")));
    }

    private void reqResponseDtoListTest(String url) throws Exception {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(responseDto1.getDescription(), responseDto2.getDescription())))
                .andExpect(jsonPath("$[*].created", containsInAnyOrder(responseDto1.getCreated().toString(), responseDto2.getCreated().toString())));
    }
}
