package ru.practicum.shareit.booking.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.MappingBooking;
import ru.practicum.shareit.booking.data.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.MappingItem;
import ru.practicum.shareit.item.data.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.MappingBooker;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.booking.ValidationBooking;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.item.ValidationItem;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    public ValidationBooking validBooking;
    @Mock
    private ValidationUser validUser;
    @Mock
    private ValidationItem validItem;
    @Mock
    private BookingRepository bookingRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ItemRepository itemRepo;

    private BookingServiceImpl bookingService;
    private BookingResponseDto booking1;
    private BookingResponseDto booking2;
    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);
    private Item item1;
    private Item item2;
    private List<Item> response;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(
                validUser,
                validItem,
                validBooking,
                bookingRepo,
                itemRepo,
                userRepo
        );

        item1 = new Item(4L,"Mike","test1",1L,true,null,List.of());
        item2 = new Item(5L,"Gleb","test2",1L,true,null,List.of());

        booking1 = BookingResponseDto.builder()
                .id(1L)
                .booker(MappingBooker.mapToBooker(2L))
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(MappingItem.mapToItemDto(item1))
                .build();

        booking2 = BookingResponseDto.builder()
                .id(2L)
                .start(start.plusDays(3))
                .end(end.plusDays(5))
                .status(BookingStatus.REJECTED)
                .item(MappingItem.mapToItemDto(item2))
                .booker(MappingBooker.mapToBooker(3L))
                .build();

        response = List.of(item1, item2);
    }

    @Test
    void createBookingWhereStatusNullTest() {
        BookingRequestDto bookingRequest = new BookingRequestDto(1L, start, end, null);
        Booking bookingAfterSave = new Booking(1L, item1, 2L, start, end, 0);

        when(validBooking.validationBookingDto(any(), anyLong())).thenReturn(item1);
        when(bookingRepo.save(any())).thenReturn(bookingAfterSave);

        BookingResponseDto booking = bookingService.addBooking(2L, bookingRequest);

        testBooking(booking, booking1);
    }

    @Test
    void createBookingWhereStatusNotNullTest() {
        BookingRequestDto bookingRequest = new BookingRequestDto(5L, start.plusDays(3), end.plusDays(5), BookingStatus.REJECTED);
        Booking bookingAfterSave = new Booking(2L, item2, 3L, start.plusDays(3), end.plusDays(5), 2);

        when(validBooking.validationBookingDto(any(), anyLong())).thenReturn(item2);
        when(bookingRepo.save(any())).thenReturn(bookingAfterSave);

        BookingResponseDto booking = bookingService.addBooking(2L, bookingRequest);

        testBooking(booking, booking2);
    }

    @Test
    void createBookingWhereUserNotFoundException() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(1L, any())
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void createBookingNotFoundExceptionTest() {
        when(validBooking.validationBookingDto(any(), anyLong())).thenThrow(
                new NotFoundException("ID собственника и арендатора совпадают"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(1L, any())
        );
        Assertions.assertEquals("ID собственника и арендатора совпадают", exception.getMessage());
    }

    @Test
    void createBookingBadRequestExceptionTest() {
        when(validBooking.validationBookingDto(any(), anyLong())).thenThrow(
                new BadRequestException("Бронирование вещи недоступно"));

        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.addBooking(1L, any())
        );
        Assertions.assertEquals("Бронирование вещи недоступно", exception.getMessage());
    }

    @Test
    void approveBookingWithApprovedStatusTest() {
        Booking booking = new Booking(1L, item1, 2L, start, end, 0);
        Booking bookingResult = new Booking(1L, item1, 2L, start, end, 1);
        User user = new User(1L, "test", "test@mail.com");

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);
        when(bookingRepo.save(any())).thenReturn(bookingResult);

        BookingResponseDto booking1 = bookingService.approvedBooking(1L, booking.getId(), true);

        assertThat(booking1.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approveBookingWithRejectedStatusTest() {
        Booking booking = new Booking(1L, item1, 2L, start, end, 0);
        Booking bookingResult = new Booking(1L, item1, 2L, start, end, 2);
        User user = new User(1L, "test", "test@mail.com");

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);
        when(bookingRepo.save(any())).thenReturn(bookingResult);

        BookingResponseDto booking1 = bookingService.approvedBooking(1L, booking.getId(), false);

        assertThat(booking1.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void approveBookingBadRequestExceptionTestOne() {
        Booking booking = new Booking(1L, item1, 2L, start, end, 1);
        User user = new User(2L, "test", "test@mail.com");

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);

        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.approvedBooking(1L, booking.getId(), true)
        );
        Assertions.assertEquals("Бронирование уже подтверждено", exception.getMessage());
    }

    @Test
    void approveBookingBadRequestExceptionTestTwo() {
        Booking booking = new Booking(1L, item1, 2L, start, end, 1);
        User user = new User(2L, "test", "test@mail.com");

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);

        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.approvedBooking(user.getId(), booking.getId(), false)
        );
        Assertions.assertEquals("Бронирование может подтверждать только собственник вещи", exception.getMessage());
    }

    @Test
    void approveBookingBadRequestExceptionTestThree() {
        final BadRequestException exception = Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.approvedBooking(1L, anyLong(), false)
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void approveValidBookingNotFoundExceptionTest() {
        User user = new User(1L, "test", "test@mail.com");

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenThrow(new NotFoundException("Бронирование с ID 1 не найдено"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.approvedBooking(1L, 1L, true)
        );
        Assertions.assertEquals("Бронирование с ID 1 не найдено", exception.getMessage());
    }

    @Test
    void approveValidItemNotFoundExceptionTest() {
        User user = new User(1L, "test", "test@mail.com");
        Booking booking = new Booking(1L, item1, 2L, start, end, 0);

        when(userRepo.findById(anyLong())).thenReturn(Optional.of(user));
        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);
        when(validItem.validationItemByUser(item1.getId(), user.getId()))
                .thenThrow(new NotFoundException(item1.getName() + " не принадлежит пользователю с ID " + user.getId()));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.approvedBooking(1L, 1L, true)
        );
        Assertions.assertEquals(item1.getName() + " не принадлежит пользователю с ID " + user.getId(), exception.getMessage());
    }

    @Test
    void findBookingByIdTest() {
        Booking booking = new Booking(1L, item1, 2L, start, end, 0);

        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);

        BookingResponseDto bookingResponseDto = bookingService.findBookingById(2L, booking.getId());

        testBooking(bookingResponseDto, booking1);
    }

    @Test
    void findBookingByIdValidBookingNotFoundExceptionTest() {
        when(validBooking.validationBookingById(anyLong())).thenThrow(new NotFoundException("Бронирование с ID 1 не найдено"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findBookingById(1L, 1L)
        );
        Assertions.assertEquals("Бронирование с ID 1 не найдено", exception.getMessage());
    }

    @Test
    void findBookingByIdValidItemNotFoundExceptionTest() {
        Booking booking = new Booking(1L, item1, 1L, start, end, 0);

        when(validBooking.validationBookingById(anyLong())).thenReturn(booking);
        when(validItem.validationItemByUser(anyLong(), anyLong()))
                .thenThrow(new NotFoundException(item1.getName() + " не принадлежит пользователю с ID " + 1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.findBookingById(2L, 2L)
        );
        Assertions.assertEquals(item1.getName() + " не принадлежит пользователю с ID " + 1, exception.getMessage());
    }

    @Test
    void getAllBookingsByUserTest() {
        Booking bookingTest1 = new Booking(1L, item1, 2L, start.plusDays(1), end.plusDays(3), 0);
        Booking bookingTest2 = new Booking(2L, item1, 2L, start.plusDays(7), end.plusDays(10), 0);
        List<Booking> bookings = List.of(bookingTest1, bookingTest2);

        when(bookingRepo.findAllBookingByBookerIdOrderByStartTimeDesc(bookingTest1.getBookerId()))
                .thenReturn(bookings);

        Collection<BookingResponseDto> bookingResponse = bookings.stream().map(MappingBooking::mapToBookingResponseDto).toList();
        Collection<BookingResponseDto> bookingResult = bookingService.getAllBookingsByUser(2L, "FUTURE");

        assertThat(bookingResult.size(), equalTo(2));
        testBookingList(bookingResponse, bookingResult);
    }

    @Test
    void getAllBookingsByUserValidUserTest() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllBookingsByUser(1L, any())
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void getAllBookingsByItemsByUserTest() {
        Booking bookingTest1 = new Booking(1L, item1, 2L, start, end, 2);
        Booking bookingTest2 = new Booking(2L, item2, 2L, start, end, 2);
        List<Booking> bookings = List.of(bookingTest1, bookingTest2);

        when(itemRepo.findByOwnerId(anyLong())).thenReturn(response);
        when(bookingRepo.findAllBookingByItemIdInOrderByStartTimeDesc(anyList())).thenReturn(bookings);

        Collection<BookingResponseDto> bookingResponse = bookings.stream().map(MappingBooking::mapToBookingResponseDto).toList();
        Collection<BookingResponseDto> bookingResult = bookingService.getAllBookingsByItemsByUser(2L, "REJECTED");

        assertThat(bookingResult.size(), equalTo(2));
        testBookingList(bookingResponse, bookingResult);
    }

    @Test
    void getAllBookingsByItemsByUserValidUserTest() {
        when(validUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllBookingsByItemsByUser(1L, any())
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    private void testItemFromBookingDto(ItemDto itemDto, ItemDto itemDtoDebug) {
        assertThat(itemDto.getName(), equalTo(itemDtoDebug.getName()));
        assertThat(itemDto.getDescription(), equalTo(itemDtoDebug.getDescription()));
        assertThat(itemDto.getRequestId(), equalTo(itemDtoDebug.getRequestId()));
        assertThat(itemDto.getAvailable(), equalTo(itemDtoDebug.getAvailable()));
    }

    private void testBooking(BookingResponseDto booking, BookingResponseDto bookingResult) {
        assertThat(booking.getId(), equalTo(bookingResult.getId()));
        assertThat(booking.getStart(), equalTo(bookingResult.getStart()));
        assertThat(booking.getStatus(), equalTo(bookingResult.getStatus()));
        assertThat(booking.getBooker().getId(), equalTo(bookingResult.getBooker().getId()));
        assertThat(booking.getStatus(), equalTo(bookingResult.getStatus()));
        testItemFromBookingDto(booking.getItem(), bookingResult.getItem());
    }

    private void testBookingList(Collection<BookingResponseDto> bookingResponse, Collection<BookingResponseDto> bookingResult) {
        bookingResponse.forEach(
                booking -> assertThat(bookingResult, hasItem(allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("start", equalTo(booking.getStart())),
                        hasProperty("end", equalTo(booking.getEnd())),
                        hasProperty("status", equalTo(booking.getStatus())),
                        hasProperty("item", allOf(
                                hasProperty("id", equalTo(booking.getItem().getId())))),
                        hasProperty("booker", allOf(
                                hasProperty("id", equalTo(booking.getBooker().getId())))
                        )))
                )
        );
    }
}
