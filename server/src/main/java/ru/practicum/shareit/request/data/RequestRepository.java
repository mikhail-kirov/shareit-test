package ru.practicum.shareit.request.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    Request findById(long id);

    List<Request> findAllByUserIdOrderByCreatedDesc(long userId);

    @Query("select req " +
            "from Request as req " +
            "left join req.items as it " +
            "where req.userId <> ?1 " +
            "order by req.created desc " +
            "limit ?2 offset ?3")
    List<Request> findAllByOtherUsers(long userId, int size, int from);
}
