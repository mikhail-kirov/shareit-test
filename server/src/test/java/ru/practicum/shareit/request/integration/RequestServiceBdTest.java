package ru.practicum.shareit.request.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.MappingRequest;
import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(properties = "jdbc.url=jdbc:h2:mem:shareit://localhost:9080/test",
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RequestServiceBdTest {

    private final RequestServiceImpl requestService;
    private final UserServiceImpl userService;
    private final EntityManager em;
    private User user;

    @BeforeEach
    void setUp() {

        user = new User(null, "Mike", "test1@mail.com");
        em.persist(user);

        List<ReqRequestDto> requests = List.of(new ReqRequestDto("sdfgsdfg"),
                                               new ReqRequestDto("sdthdrhu"));

        List<Request> requestList = requests.stream()
                .map(req -> MappingRequest.mapToRequest(1L, req))
                .toList();
        requestList.forEach(em::persist);

        ReqRequestDto requestDto = new ReqRequestDto("yfghghfd");
        em.persist(MappingRequest.mapToRequest(2L, requestDto));

        ItemDto itemDto = ItemDto.builder()
                .name("sdfas")
                .description("tujfyu")
                .available(true)
                .requestId(1L)
                .build();
        em.persist(MappingItem.mapToItem(itemDto, 2L));
        em.flush();
    }

    @Test
    void createRequestTest() {
        User userTest = new User(null,"Max", "test3@mail.com");
        User user1 = userService.addUser(userTest);

        ReqResponseDto responseDto = createRequest("description", user1.getId());
        Request result = setRequestFromBd(responseDto.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(responseDto.getId()));
        assertThat(result.getDescription(), is(responseDto.getDescription()));
        assertThat(result.getCreated(), is(responseDto.getCreated()));
        assertThat(result.getItems(), hasSize(0));
    }

    @Test
    void getRequestByIdTest() {
        User userTest = new User(null,"Max", "test3@mail.com");
        User user1 = userService.addUser(userTest);

        ReqResponseDto responseDto = createRequest("test", user1.getId());
        Request result = setRequestFromBd(responseDto.getId());

        ReqResponseDto reqResponseDto = requestService.getRequestById(responseDto.getId(), user1.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(reqResponseDto.getId()));
        assertThat(result.getDescription(), is(reqResponseDto.getDescription()));
        assertThat(result.getCreated(), is(reqResponseDto.getCreated()));
        assertThat(result.getItems(), hasSize(0));
    }

    @Test
    void getRequestsByUserIdTest() {
        List<ReqResponseDto> reqResponse = requestService.getRequestsByUserId(user.getId());

        TypedQuery<Request> query = em.createQuery("SELECT r FROM Request r WHERE r.userId = :userId", Request.class);
        List<Request> result = query.setParameter("userId", user.getId()).getResultList();

        List<ReqResponseDto> reqResponseQuery = result.stream().map(MappingRequest::mapToReqResponseDto).toList();

        assertThat(reqResponse, hasSize(result.size()));

        for (ReqResponseDto reqResponseDto : reqResponseQuery) {
            assertThat(reqResponse, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(reqResponseDto.getDescription())),
                    hasProperty("created", equalTo(reqResponseDto.getCreated())),
                    hasProperty("items", equalTo(List.of()))
            )));
        }
    }

    @Test
    void getRequestsByOtherUserIdTest() {
        List<ReqResponseDto> responseDtoList = requestService.getRequestsByOtherUsers(new GetAllRequestDto(user.getId(), 0, 5));

        TypedQuery<Request> query = em.createQuery("SELECT r FROM Request r WHERE r.userId <> :userId " +
                "order by r.created desc limit 5 offset 0", Request.class);
        List<Request> result = query.setParameter("userId", user.getId()).getResultList();

        List<ReqResponseDto> reqResponseQuery = result.stream().map(MappingRequest::mapToReqResponseDto).toList();

        for (ReqResponseDto reqResponseDto : reqResponseQuery) {
            assertThat(responseDtoList, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(reqResponseDto.getDescription())),
                    hasProperty("created", equalTo(reqResponseDto.getCreated())),
                    hasProperty("items", equalTo(List.of()))
            )));
        }
    }

    private ReqResponseDto createRequest(String description, Long userId) {
        ReqRequestDto requestDto = new ReqRequestDto(description);
        return requestService.createRequest(userId, requestDto);
    }

    private Request setRequestFromBd(Long id) {
        TypedQuery<Request> query = em.createQuery("SELECT r FROM Request r WHERE r.id = :id", Request.class);
        return query.setParameter("id", id).getSingleResult();
    }
}
