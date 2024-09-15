package ru.practicum.shareit.booking.data;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllBookingByBookerIdOrderByStartTimeDesc(Long bookerId);

    List<Booking> findAllBookingByBookerIdAndItemOrderByStartTimeDesc(Long bookerId, Item item);

    List<Booking> findAllBookingByItemIdInOrderByStartTimeDesc(List<Long> itemsId);

    List<Booking> findAllBookingByItemIdOrderByStartTimeAsc(Long itemId);
}
