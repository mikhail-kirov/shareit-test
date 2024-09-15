package ru.practicum.shareit.request;

import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.model.Request;

import java.time.LocalDateTime;
import java.util.List;

public class MappingRequest {

    public static ReqResponseDto mapToReqResponseDto(Request request) {
        return ReqResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemsForRequest(request))
                .build();
    }

    public static List<ReqResponseDto> mapToReqResponseDto(List<Request> request) {
        return request.stream().map(MappingRequest::mapToReqResponseDto).toList();
    }

    public static Request mapToRequest(long userId, ReqRequestDto reqRequestDto) {
        Request request = new Request();
        request.setUserId(userId);
        request.setDescription(reqRequestDto.getDescription());
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private static List<ItemForRequestDto> itemsForRequest(Request request) {
        return request.getItems().stream().map(MappingItem::mapToItemForRequestDto).toList();
    }
}
