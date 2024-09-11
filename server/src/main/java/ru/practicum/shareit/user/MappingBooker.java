package ru.practicum.shareit.user;

public class MappingBooker {

    public static Booker mapToBooker(Long id) {
        return Booker.builder()
                .id(id)
                .build();
    }
}
