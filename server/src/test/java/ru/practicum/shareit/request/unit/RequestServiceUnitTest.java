package ru.practicum.shareit.request.unit;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.data.RequestRepository;
import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestServiceUnitTest {
    @Mock
    private ValidationUser validUser;
    @Mock
    private RequestRepository requestRepo;

    private RequestServiceImpl requestService;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime createdDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private Request request1;
    private List<Request> requests;
    private List<ReqResponseDto> responseDtoList;

    @BeforeEach
    void setUp() {
        requestService = new RequestServiceImpl(requestRepo, validUser);

        request1 = new Request(1L, "test1", createdDate, 1L, List.of());
        Request request2 = new Request(2L, "test2", createdDate.plusDays(5), 1L, List.of());
        requests = List.of(request1, request2);

        ReqResponseDto responseDto1 = ReqResponseDto.builder()
                .id(1L)
                .description("test1")
                .created(createdDate)
                .build();
        ReqResponseDto responseDto2 = ReqResponseDto.builder()
                .id(2L)
                .description("test2")
                .created(createdDate.plusDays(5))
                .build();
        responseDtoList = List.of(responseDto1, responseDto2);
    }

    @Test
    void createRequest() {
        ReqRequestDto requestDto = new ReqRequestDto("test");
        when(requestRepo.save(any())).thenReturn(request1);

        ReqResponseDto responseDto = requestService.createRequest(1L, requestDto);

        assertThat(responseDto.getId(), equalTo(request1.getId()));
        assertThat(responseDto.getDescription(), equalTo(request1.getDescription()));
        assertThat(responseDto.getCreated(), equalTo(request1.getCreated()));
    }

    @Test
    void testCreateRequestWhenException() {
        ReqRequestDto requestDto = new ReqRequestDto("test");
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.createRequest(anyLong(), requestDto)
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void getRequest() {
        Item item1 = new Item(7L, "Mik", "test1", 1L, true, 2L, List.of());
        Item item2 = new Item(9L, "Mark", "test2", 11L, false, 9L, List.of());
        request1.setItems(List.of(item1, item2));

        ItemForRequestDto itemForRequest1 = ItemForRequestDto.builder()
                .id(7L)
                .ownerId(1L)
                .name("Mik")
                .build();
        ItemForRequestDto itemForRequest2 = ItemForRequestDto.builder()
                .id(9L)
                .ownerId(11L)
                .name("Mark")
                .build();
        List<ItemForRequestDto> itemForRequestDto = List.of(itemForRequest1, itemForRequest2);

        when(requestRepo.findById(anyLong())).thenReturn(request1);

        ReqResponseDto responseDto = requestService.getRequestById(1L, 1L);

        assertThat(responseDto.getId(), equalTo(request1.getId()));
        assertThat(responseDto.getDescription(), equalTo(request1.getDescription()));
        assertThat(responseDto.getCreated(), equalTo(request1.getCreated()));
        responseDto.getItems().forEach(
                item -> assertThat(itemForRequestDto, hasItem(allOf(
                        hasProperty("id", equalTo(item.getId())),
                        hasProperty("ownerId", equalTo(item.getOwnerId())),
                        hasProperty("name", equalTo(item.getName()))
                )))
        );
    }

    @Test
    void getRequestNotFoundException() {

        when(requestRepo.findById(anyLong())).thenThrow(new EmptyResultDataAccessException("test", 0));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestById(1L, anyLong())
        );
        Assertions.assertEquals("Запрос с ID 1 не найден", exception.getMessage());
    }

    @Test
    void getRequestValidUserNotFoundException() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestById(anyLong(), 1L)
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void getRequestsByUserId() {
        when(requestRepo.findAllByUserIdOrderByCreatedDesc(anyLong())).thenReturn(requests);
        List<ReqResponseDto> responseDto = requestService.getRequestsByUserId(1L);
        assertThat(responseDto, hasSize(responseDtoList.size()));
        requestListTest(responseDto);
    }

    @Test
    void getRequestsByUserIdValidUserNotFoundException() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestsByUserId(1L)
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void getRequestsByOtherUsers() {
        when(requestRepo.findAllByOtherUsers(anyLong(), anyInt(), anyInt())).thenReturn(requests);
        List<ReqResponseDto> responseDto = requestService.getRequestsByOtherUsers(new GetAllRequestDto(1L, 2, 3));
        assertThat(responseDto, hasSize(responseDtoList.size()));
        requestListTest(responseDto);
    }

    @Test
    void getRequestsByOtherUsersValidUserNotFoundException() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 2 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestsByOtherUsers(new GetAllRequestDto(2L, 2, 3))
        );
        Assertions.assertEquals("Пользователь с ID 2 не зарегистрирован", exception.getMessage());
    }

    private void requestListTest(List<ReqResponseDto> responseDto) {
        responseDtoList.forEach(
                req -> assertThat(responseDto, hasItem(allOf(
                        hasProperty("id", equalTo(req.getId())),
                        hasProperty("description", equalTo(req.getDescription())),
                        hasProperty("created", equalTo(req.getCreated()))
                )))
        );
    }
}
