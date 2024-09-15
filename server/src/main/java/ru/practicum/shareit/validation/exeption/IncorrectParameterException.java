package ru.practicum.shareit.validation.exeption;

public class IncorrectParameterException extends RuntimeException {
    public IncorrectParameterException(final String message) {
        super(message);
    }
}
