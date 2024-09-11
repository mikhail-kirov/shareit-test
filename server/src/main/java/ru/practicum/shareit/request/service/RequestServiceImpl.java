package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.MappingRequest;
import ru.practicum.shareit.request.data.RequestRepository;
import ru.practicum.shareit.request.dto.GetAllRequestDto;
import ru.practicum.shareit.request.dto.ReqRequestDto;
import ru.practicum.shareit.request.dto.ReqResponseDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final ValidationUser validationUser;

    @Override
    public ReqResponseDto createRequest(long userId, ReqRequestDto req) {
        validationUser.validationUserById(userId);
        Request request = requestRepository.save(MappingRequest.mapToRequest(userId, req));
        log.info("Запрос добавлен, ID {}", request.getId());
        return MappingRequest.mapToReqResponseDto(request);
    }

    @Override
    public ReqResponseDto getRequestById(long reqId, long userId) {
        validationUser.validationUserById(userId);
        Request request;
        try {
            request = requestRepository.findById(reqId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Запрос с ID " + reqId + " не найден");
        }
        log.info("Запрос с ID {} найден и отправлен", reqId);
        return MappingRequest.mapToReqResponseDto(request);
    }

    @Override
    public List<ReqResponseDto> getRequestsByUserId(long userId) {
        validationUser.validationUserById(userId);
        List<Request> responseDtoList = requestRepository.findAllByUserIdOrderByCreatedDesc(userId);
        log.info("Отправлен список всех запросов пользователя с ID {}", userId);
        return MappingRequest.mapToReqResponseDto(responseDtoList);
    }

    @Override
    public List<ReqResponseDto> getRequestsByOtherUsers(GetAllRequestDto requestDto) {
        long userId = requestDto.getUserId();
        validationUser.validationUserById(userId);
        List<Request> responseDtoList = requestRepository.findAllByOtherUsers(userId,
                requestDto.getSize(),
                requestDto.getFrom());
        log.info("Для пользователя с ID {} отправлен список запросов других пользователей", userId);
        return MappingRequest.mapToReqResponseDto(responseDtoList);
    }
}
