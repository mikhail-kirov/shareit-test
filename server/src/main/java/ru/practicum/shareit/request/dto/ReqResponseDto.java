package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class ReqResponseDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemForRequestDto> items;
}
