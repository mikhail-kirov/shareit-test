package ru.practicum.shareit.user.mockMVC;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.exeption.IncorrectParameterException;
import ru.practicum.shareit.validation.exeption.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig({UserController.class, UserControllerTestConfig.class, ShareItServer.class})
public class UserControllerWithContextTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final UserService userService;
    private MockMvc mockMvc;
    private User user1;
    private User user2;

    @Autowired
    public UserControllerWithContextTest(UserService userService) {
        this.userService = userService;
    }

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        user1 = new User(1L,"John", "test1@mail.com");
        user2 = new User(2L,"Jane", "test2@mail.com");
    }

    @Test
    void createUser() throws Exception {
        when(userService.addUser(any())).thenReturn(user1);

        mockMvc.perform(post("/users")
            .content(mapper.writeValueAsString(user1))
            .characterEncoding(StandardCharsets.UTF_8)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
    }

    @Test
    void updateUser() throws Exception {
        User userUpdate = new User(1L,"Mike", "test1@mail.com");
        when(userService.updateUser(anyLong(), any())).thenReturn(userUpdate);

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userUpdate.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
    }

    @Test
    void getAllUsers() throws Exception {
        List<User> users = List.of(user1, user2);
        when(userService.getUsers()).thenReturn(users);

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(user1.getName(), user2.getName())))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(user1.getEmail(), user2.getEmail())));
    }

    @Test
    void getUser() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(user1);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));
    }

    @Test
    void testUserByIdNotFoundException() throws Exception {
        when(userService.getUserById(2L)).thenThrow(new NotFoundException("Пользователь с ID 2 не зарегистрирован"));

        mockMvc.perform(get("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 2 не зарегистрирован")));
    }

    @Test
    void testCreateUserIncorrectParameterException() throws Exception {
            when(userService.addUser(any()))
                    .thenThrow(new IncorrectParameterException("Пользователь с указанным email уже зарегистрирован"));

            mockMvc.perform(post("/users")
                            .content(mapper.writeValueAsString(user1))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error", is("Пользователь с указанным email уже зарегистрирован")));
    }

    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/1")
                        .content(mapper.writeValueAsString(user1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
