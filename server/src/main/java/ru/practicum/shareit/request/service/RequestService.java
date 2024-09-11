package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;

import java.util.List;

public interface RequestService {

    ReqResponseDto createRequest(long userId, ReqRequestDto req);

    ReqResponseDto getRequestById(long reqId, long userId);

    List<ReqResponseDto> getRequestsByUserId(long userId);

    List<ReqResponseDto> getRequestsByOtherUsers(GetAllRequestDto requestDto);
}
