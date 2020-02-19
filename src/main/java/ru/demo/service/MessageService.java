package ru.demo.service;

import ru.demo.model.Message;

import java.util.List;

public interface MessageService
{
    void handleMessages(List<Message> messages);
}
