package ru.practicum.shareit.validation.exeption;

public class NotFoundException extends RuntimeException {
    public NotFoundException(final String message) {
        super(message);
    }
}
