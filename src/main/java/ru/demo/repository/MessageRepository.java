package ru.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.demo.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
