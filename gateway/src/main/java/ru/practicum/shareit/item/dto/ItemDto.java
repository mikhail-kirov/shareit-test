package ru.practicum.shareit.item.dto;

import lombok.*;


@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
