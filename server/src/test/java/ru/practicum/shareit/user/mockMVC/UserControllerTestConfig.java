package ru.practicum.shareit.user.mockMVC;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.Mockito.mock;

@Configuration
public class UserControllerTestConfig {
    @Bean
    public UserService userService() {
        return mock(UserService.class);
    }
}
