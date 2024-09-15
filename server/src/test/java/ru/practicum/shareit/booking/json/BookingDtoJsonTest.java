package ru.practicum.shareit.booking.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.MappingBooker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingDtoJsonTest {
    private final JacksonTester<BookingRequestDto> jsonRequest;
    private final JacksonTester<BookingResponseDto> jsonResponse;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime end = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

    @Test
    void testBookingRequestDto() throws Exception {
        BookingRequestDto bookingRequest = new BookingRequestDto(
                1L,
                start,
                end,
                BookingStatus.APPROVED
        );

        JsonContent<BookingRequestDto> result = jsonRequest.write(bookingRequest);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.APPROVED.toString());
    }

    @Test
    void testBookingResponseDto() throws Exception {
        Item item = new Item(4L,"TEST","test1",1L,true,5L, List.of());
        BookingResponseDto bookingResponse = BookingResponseDto.builder()
                .id(1L)
                .booker(MappingBooker.mapToBooker(2L))
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(MappingItem.mapToItemDto(item))
                .build();

        JsonContent<BookingResponseDto> result = jsonResponse.write(bookingResponse);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.WAITING.toString());

        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(4);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(5);
        assertThat(result).extractingJsonPathArrayValue("$.item.comments").isEqualTo(List.of());
    }
}
