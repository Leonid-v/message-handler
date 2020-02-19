package ru.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.demo.repository.MessageRepository;
import ru.demo.service.MessageService;
import ru.demo.service.MessageServiceImpl;

@Configuration
public class ServiceConfig {

    @Bean
    public MessageService messageService(MessageRepository messageRepository) {
        return new MessageServiceImpl(messageRepository);
    }
}
