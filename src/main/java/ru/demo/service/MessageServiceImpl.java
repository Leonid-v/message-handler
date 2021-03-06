package ru.demo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import ru.demo.model.Message;
import ru.demo.repository.MessageRepository;


import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.List;

public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LogManager.getLogger(MessageServiceImpl.class);

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void handleMessages(List<Message> messages) {

        try {
            messageRepository.saveAll(messages);
        } catch (JpaSystemException e) {
            logger.error("Connection exception", e);
            throw new DataBaseNotAvailable();
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException constraintViolationException = (ConstraintViolationException) e.getCause();
                if(constraintViolationException.getCause() instanceof BatchUpdateException){
                    BatchUpdateException batchUpdateException = (BatchUpdateException) constraintViolationException.getCause();
                    var updateCounts = batchUpdateException.getUpdateCounts();
                    for (int i = 0; i < updateCounts.length; i++) {
                        if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                            logger.error("Failed to save message {}", messages.get(i));
                        }
                    }
                }
            }
            logger.error(e);
        }
    }
}
