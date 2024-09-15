package ru.practicum.shareit.booking.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.MappingBooking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(properties = "jdbc.url=jdbc:h2:mem:shareit://localhost:8070/test",
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BookingServiceBdTest {

    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserServiceImpl userService;
    private final EntityManager em;
    private final LocalDateTime start = LocalDateTime.now();
    private final LocalDateTime end = start.plusDays(3);
    private ItemDto itemDto;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        User us1 = new User(null,"Mike", "test1@mail.com");
        User us2 = new User(null,"Mik", "test2@mail.com");
        user1 = userService.addUser(us1);
        user2 = userService.addUser(us2);

        ItemDto itemDto1 = ItemDto.builder()
                .name("dsfg")
                .description("sdfsd")
                .available(true)
                .build();
        itemDto = itemService.addItem(user1.getId(), itemDto1);
        Item item = MappingItem.mapToItem(itemDto, user1.getId());
        item.setId(itemDto.getId());

        BookingRequestDto bookingDto = new BookingRequestDto(1L,start,end, BookingStatus.WAITING);
        em.persist(MappingBooking.mapToBooking(2L, bookingDto, item));
        em.flush();
    }

    @Test
    void saveBookingTest() {
        BookingRequestDto bookingDto1 = new BookingRequestDto(itemDto.getId(),start.plusDays(1),end.plusDays(4), BookingStatus.REJECTED);
        BookingResponseDto bookingResponse = bookingService.addBooking(user2.getId(), bookingDto1);

        Booking booking = setBookingFromBd(bookingResponse.getId());

        assertThat(booking.getId(), notNullValue());
        assertEquals(booking.getBookerId(), user2.getId(), "ID арендатора не сохранилось в БД");
        assertThat(booking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(booking.getStartTime(), equalTo(bookingDto1.getStart()));
        assertThat(booking.getEndTime(), equalTo(bookingDto1.getEnd()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.REJECTED.ordinal()));
    }

    @Test
    void findBookingTest() {
        Long bookingResponseId = saveBooking();
        bookingService.findBookingById(user1.getId(), bookingResponseId);

        Booking booking = setBookingFromBd(bookingResponseId);

        assertThat(booking.getId(), notNullValue());
        assertEquals(booking.getBookerId(), user2.getId(), "ID арендатора не сохранилось в БД");
        assertThat(booking.getStartTime(), equalTo(start));
        assertThat(booking.getEndTime(), equalTo(end));
        assertThat(booking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.REJECTED.ordinal()));
    }

    @Test
    void approveBookingTest() {
        Long bookingResponseId = saveBooking();
        bookingService.approvedBooking(user1.getId(), bookingResponseId, true);

        Booking booking = setBookingFromBd(bookingResponseId);

        assertThat(booking.getStatus(), equalTo(BookingStatus.APPROVED.ordinal()));
    }

    private Long saveBooking() {
        BookingRequestDto bookingDto1 = new BookingRequestDto(itemDto.getId(),start,end, BookingStatus.REJECTED);
        BookingResponseDto bookingResponse = bookingService.addBooking(user2.getId(), bookingDto1);
        return bookingResponse.getId();
    }

    private Booking setBookingFromBd(Long bookingId) {
        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        return query.setParameter("id", bookingId).getSingleResult();
    }
}
