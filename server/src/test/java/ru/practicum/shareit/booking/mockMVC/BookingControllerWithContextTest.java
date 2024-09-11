package ru.practicum.shareit.booking.mockMVC;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.MappingBooker;
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

@WebMvcTest(BookingController.class)
public class BookingControllerWithContextTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mockMvc;
    private BookingRequestDto bookingRequest;
    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
    private BookingResponseDto booking1;
    private BookingResponseDto booking2;
    private List<BookingResponseDto> response;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequestDto(1L, start, end, null);

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("sdfgbsdf")
                .requestId(1L)
                .description("sdfgdf")
                .build();

        booking1 = BookingResponseDto.builder()
                .id(1L)
                .booker(MappingBooker.mapToBooker(2L))
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(item)
                .build();

        booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(start.plusDays(3))
                .end(end.plusDays(5))
                .status(BookingStatus.REJECTED)
                .booker(MappingBooker.mapToBooker(3L))
                .build();

        response = List.of(booking1, booking2);
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenReturn(booking1);

        mockMvc.perform(post("/bookings")
                .content(mapper.writeValueAsString(bookingRequest))
                .header("X-Sharer-User-Id", 1L)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.start", is(booking1.getStart().toString())))
                    .andExpect(jsonPath("$.end", is(booking1.getEnd().toString())))
                    .andExpect(jsonPath("$.status", is(booking1.getStatus().name())))
                    .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class));
    }

    @Test
    void updateBookingStatus() throws Exception {
        booking1.setStatus(BookingStatus.APPROVED);
        when(bookingService.approvedBooking(anyLong(),anyLong(),anyBoolean())).thenReturn(booking1);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start", is(booking1.getStart().toString())))
                .andExpect(jsonPath("$.end", is(booking1.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class));
    }

    @Test
    void getBooking() throws Exception {
        when(bookingService.findBookingById(anyLong(),anyLong())).thenReturn(booking1);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.start", is(booking1.getStart().toString())))
                .andExpect(jsonPath("$.end", is(booking1.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(booking1.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(booking1.getItem().getId()), Long.class));
    }

    @Test
    void getAllBookingsByUser() throws Exception {
        String url = "/bookings?state=all";
        when(bookingService.getAllBookingsByUser(anyLong(), anyString())).thenReturn(response);
        mockMvcBookingListTest(url);
    }

    @Test
    void getAllBookingsByOwner() throws Exception {
        String url = "/bookings/owner?state=all";
        when(bookingService.getAllBookingsByItemsByUser(anyLong(), anyString())).thenReturn(response);
        mockMvcBookingListTest(url);
    }

    @Test
    void createBookingNotFoundException() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(new NotFoundException("Бронирование с ID 1 не найдено"));

        mockMvc.perform(post("/bookings")
                .content(mapper.writeValueAsString(bookingRequest))
                .header("X-Sharer-User-Id", 1L)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error", is("Бронирование с ID 1 не найдено")));
    }

    @Test
    void createBookingBadRequestException() throws Exception {
        when(bookingService.addBooking(anyLong(), any())).thenThrow(new BadRequestException("Бронирование вещи недоступно"));

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequest))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Бронирование вещи недоступно")));
    }

    private void mockMvcBookingListTest(String url) throws Exception {
        mockMvc.perform(get(url)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].start", containsInAnyOrder(booking1.getStart().toString(), booking2.getStart().toString())))
                .andExpect(jsonPath("$[*].end", containsInAnyOrder(booking1.getEnd().toString(), booking2.getEnd().toString())))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder(booking1.getStatus().name(), booking2.getStatus().name())))
                .andExpect(jsonPath("$[*].booker.id", containsInAnyOrder(2, 3)));
    }
}
