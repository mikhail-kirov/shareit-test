package ru.practicum.shareit.validation.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.validation.exeption.BadRequestException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationComment {
    public void validateComment(String comment) {
        if (comment == null || comment.isEmpty()) {
            throw new BadRequestException("Комментарий на получен");
        }
    }
}
