package ru.practicum.shareit.request.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestDtoJsonTest {
    private final JacksonTester<GetAllRequestDto> getAllRequestDto;
    private final JacksonTester<ReqRequestDto> reqRequestDto;
    private final JacksonTester<ReqResponseDto> reqResponseDto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
    private final LocalDateTime createdDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Test
    void testGetAllRequestDto() throws Exception {
        GetAllRequestDto getAllRequest = new GetAllRequestDto(1L, 2, 3);
        JsonContent<GetAllRequestDto> result = getAllRequestDto.write(getAllRequest);

        assertThat(result).extractingJsonPathNumberValue("$.userId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.from").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.size").isEqualTo(3);
    }

    @Test
    void testReqRequestDto() throws Exception {
        ReqRequestDto requestDto = new ReqRequestDto("test");
        JsonContent<ReqRequestDto> result = reqRequestDto.write(requestDto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("test");
    }

    @Test
    void testReqResponseDto() throws Exception {
        ItemForRequestDto itemForRequest = ItemForRequestDto.builder()
                .id(7L)
                .ownerId(1L)
                .name("Mike")
                .build();
        ReqResponseDto responseDto = ReqResponseDto.builder()
                .id(1L)
                .description("test")
                .created(createdDate)
                .items(List.of(itemForRequest))
                .build();

        JsonContent<ReqResponseDto> result = reqResponseDto.write(responseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("test");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(createdDate.toString());

        assertThat(result).extractingJsonPathStringValue("$.items.id", containsInAnyOrder(7));
        assertThat(result).extractingJsonPathStringValue("$.items.ownerId", containsInAnyOrder(1));
        assertThat(result).extractingJsonPathStringValue("$.items.name", containsInAnyOrder("Mike"));
    }
}
