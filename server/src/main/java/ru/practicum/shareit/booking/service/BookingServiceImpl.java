package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.MappingBooking;
import ru.practicum.shareit.booking.data.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.data.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.validation.booking.ValidationBooking;
import ru.practicum.shareit.validation.exeption.BadRequestException;
import ru.practicum.shareit.validation.item.ValidationItem;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final ValidationUser validationUser;
    private final ValidationItem validationItem;
    private final ValidationBooking validationBooking;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto addBooking(Long userId, BookingRequestDto bookingRequestDto) {
        validationUser.validationUserById(userId);
        Item item = validationBooking.validationBookingDto(bookingRequestDto, userId);
        if (bookingRequestDto.getStatus() == null) {
            bookingRequestDto.setStatus(BookingStatus.WAITING);
        }
        Booking booking = bookingRepository.save(MappingBooking.mapToBooking(userId, bookingRequestDto, item));
        return MappingBooking.mapToBookingResponseDto(booking, item);
    }

    @Override
    @Transactional
    public BookingResponseDto approvedBooking(Long userId, Long bookingId, Boolean approved) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Пользователь с ID " + userId + " не зарегистрирован"));
        Booking booking = validationBooking.validationBookingById(bookingId);
        validationItem.validationItemByUser(booking.getItem().getId(), userId);
        if (booking.getStatus().equals(1) && approved) {
            throw new BadRequestException("Бронирование уже подтверждено");
        }
        if (!booking.getItem().getOwnerId().equals(userId)) {
            throw new BadRequestException("Бронирование может подтверждать только собственник вещи");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED.ordinal());
        } else {
            booking.setStatus(BookingStatus.REJECTED.ordinal());
        }
        Booking bookingResult = bookingRepository.save(booking);
        return MappingBooking.mapToBookingResponseDto(bookingResult);
    }

    @Override
    public BookingResponseDto findBookingById(Long userId, Long bookingId) {
        Booking booking = validationBooking.validationBookingById(bookingId);
        if (!booking.getBookerId().equals(userId)) {
            validationItem.validationItemByUser(booking.getItem().getId(), userId);
        }
        return MappingBooking.mapToBookingResponseDto(booking);
    }

    @Override
    public Collection<BookingResponseDto> getAllBookingsByUser(Long userId, String state) {
        validationUser.validationUserById(userId);
        List<Booking> bookings = bookingRepository.findAllBookingByBookerIdOrderByStartTimeDesc(userId);
        return getListBookingStateDtoFromBookings(bookings, state);
    }

    @Override
    public Collection<BookingResponseDto> getAllBookingsByItemsByUser(Long userId, String state) {
        validationUser.validationUserById(userId);
        List<Long> itemsId = itemRepository.findByOwnerId(userId).stream().map(Item::getId).toList();
        List<Booking> bookings = bookingRepository.findAllBookingByItemIdInOrderByStartTimeDesc(itemsId);
        return getListBookingStateDtoFromBookings(bookings, state);
    }

    private Collection<BookingResponseDto> getListBookingStateDtoFromBookings(List<Booking> bookings, String state) {
        Collection<Booking> bookingsSort = MappingBooking.mapToBookingState(bookings, state);
        return MappingBooking.mapToBookingResponseDto(bookingsSort);
    }
}
