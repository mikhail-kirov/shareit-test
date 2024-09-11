package ru.practicum.shareit.item.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional()
@SpringBootTest(properties = "jdbc.url=jdbc:h2:mem:shareit://localhost:9090/test",
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ItemServiceBdTest {

    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    private final EntityManager em;
    private List<ItemDto> itemsDto;

    @BeforeEach
    void setUp() {
        User user1 = new User(null,"Mike", "test1@mail.com");
        em.persist(user1);

        itemsDto = List.of(
                createItemDto("dsfg", "sdfsd", true),
                createItemDto("dsfgse", "sdsdffsd", true));
        List<Item> items = itemsDto.stream()
                .map(itemDto -> MappingItem.mapToItem(itemDto, 1L))
                .toList();
        items.forEach(em::persist);
        em.flush();
    }

    @Test
    void createItemTest() {

        User userTest = new User(null,"Mik", "test2@mail.com");
        User user = userService.addUser(userTest);

        ItemDto itemDto = itemsDto.get(0);
        ItemDto itemDtoResult = itemService.addItem(user.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item result = query.setParameter("id", itemDtoResult.getId()).getSingleResult();

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), is(itemDtoResult.getName()));
        assertThat(result.getDescription(), is(itemDtoResult.getDescription()));
        assertThat(result.getAvailable(), is(itemDtoResult.getAvailable()));
    }

    @Test
    void getItemDtoByIdTest() {
        ItemDto itemDtoTest = createItemDto("sgsrth", "llkuihui", false);
        ItemDto itemDto = itemService.addItem(2L, itemDtoTest);

        ItemDto itemDtoResult = itemService.getItemDtoById(2L, itemDto.getId());

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item result = query.setParameter("id", itemDtoResult.getId()).getSingleResult();
        ItemDto itemDtoQuery = MappingItem.mapToItemDto(result);

        itemDtoListTest(List.of(itemDtoResult), List.of(itemDtoQuery));
    }

    @Test
    void getItemsDtoBySearchTest() {
        User userTest = new User(null,"Mik", "test2@mail.com");
        User user = userService.addUser(userTest);

        Collection<ItemDto> itemsDtoBySearch = itemService.getItemsDtoBySearch(user.getId(), "dsfg");

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.name ilike :name " +
                "or i.description ilike :description and i.available = :available", Item.class);
        List<Item> result = query.setParameter("name", "%dsfg%")
                                 .setParameter("description", "%dsfg%")
                                 .setParameter("available", true)
                                 .getResultList();

        List<ItemDto> itemsQuery = result.stream().map(MappingItem::mapToItemDto).toList();

        assertThat(itemsDtoBySearch, hasSize(result.size()));

        itemDtoListTest(itemsDtoBySearch, itemsQuery);
    }

    @Test
    void getItemsByUserIdTest() {
        Collection<ItemDto> itemsDtoResult = itemService.getItemsDtoByUserId(1L);

        TypedQuery<Item> query = em.createQuery("select it from Item it where it.ownerId = :id", Item.class);
        List<Item> itemsList = query.setParameter("id", 1L).getResultList();

        List<ItemDto> itemsQuery = itemsList.stream().map(MappingItem::mapToItemDto).toList();

        assertThat(itemsDtoResult, hasSize(itemsQuery.size()));

        itemDtoListTest(itemsDtoResult, itemsQuery);
    }

    private ItemDto createItemDto(String name, String description, Boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private void itemDtoListTest(Collection<ItemDto> itemDtoMethod, Collection<ItemDto> itemsDtoQuery) {
        for (ItemDto itemDto : itemsDtoQuery) {
            assertThat(itemDtoMethod, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(itemDto.getName())),
                    hasProperty("description", equalTo(itemDto.getDescription())),
                    hasProperty("available", equalTo(itemDto.getAvailable())),
                    hasProperty("requestId", equalTo(null)),
                    hasProperty("lastBooking", equalTo(null)),
                    hasProperty("nextBooking", equalTo(null)),
                    hasProperty("comments", equalTo(List.of()))
            )));
        }
    }
}
