package com.bidpoint.backend.item.repository;

import com.bidpoint.backend.item.entity.Category;
import com.bidpoint.backend.item.entity.Item;
import com.bidpoint.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item,UUID>, ItemRepositoryCustom {
    Item findItemById(UUID id);
}
