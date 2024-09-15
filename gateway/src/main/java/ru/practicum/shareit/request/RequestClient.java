package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exeption.BadRequestException;
import ru.practicum.shareit.request.dto.ReqRequestDto;

import java.util.Map;

@Service
@Slf4j
public class RequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createRequest(long userId, ReqRequestDto req) {
        isValidRequestDto(req.getDescription());
        return post("", userId, req);
    }

    public ResponseEntity<Object> getRequestById(long reqId, long userId) {
        return get("/" + reqId, userId);
    }

    public ResponseEntity<Object> getRequestsByUserId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getRequestsByOtherUsers(long userId, int from, int size) {
        Map<String, Object> parameters = Map.of("from", from, "size", size);
        return get("/all", userId, parameters);
    }

    private void isValidRequestDto(String description) {
        if (description == null || description.isEmpty()) {
            log.info("Некорректный запрос");
            throw new BadRequestException("Некорректный запрос");
        }
    }
}
