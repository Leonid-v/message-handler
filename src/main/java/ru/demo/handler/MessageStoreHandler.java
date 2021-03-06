package ru.demo.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import ru.demo.model.Message;
import ru.demo.service.MessageService;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class MessageStoreHandler {

    private static final Logger logger = LogManager.getLogger(MessageStoreHandler.class);

    private final MessageService messageService;

    @Value("${app.batchSize}")
    private Integer batchSize;

    @Value("${app.batchTimeout}")
    private Integer batchTimeout;

    public MessageStoreHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @KafkaListener(
            id = "message-store-handler",
            topics = "${app.handledMessageTopic}",
            containerFactory = "messageStoreContainerFactory"
    )
    public void handleMessages(@Payload List<ConsumerRecord<Long, Message>> messages, Acknowledgment ack) {

        logger.info("accept messages: {}", messages.stream().map(r -> r.value().getMessageId())
                .collect(toList()));

        int executedMessages = 0;
        try {

            List<ConsumerRecord<Long, Message>> toStore;
            if (messages.size() >= batchSize) {
                toStore = messages.subList(0, batchSize);
            } else if (isBatchTimedOut(messages)) {
                toStore = messages;
            } else {
                return;
            }

            List<Message> batch = toStore.stream().map(ConsumerRecord::value).collect(toList());

            logger.info("Sending to storage batch with size {}", batch.size());
            logger.info("Sending to storage messages with id's {}",
                    batch.stream().map(Message::getMessageId).collect(toList()));
            messageService.handleMessages(batch);
            executedMessages = batch.size();


        } finally {
            if (executedMessages == messages.size()) {
                ack.acknowledge();
            } else {
                ack.nack(executedMessages, batchTimeout / 4);
            }
        }


    }

    private boolean isBatchTimedOut(List<ConsumerRecord<Long, Message>> batch) {
        return System.currentTimeMillis() - batch.get(batch.size() - 1).timestamp() > batchTimeout;
    }

}
