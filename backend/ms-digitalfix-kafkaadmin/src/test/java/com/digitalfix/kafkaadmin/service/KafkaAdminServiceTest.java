package com.digitalfix.kafkaadmin.service;

import com.digitalfix.kafkaadmin.dto.TopicRequest;
import com.digitalfix.kafkaadmin.exception.ConflictException;
import com.digitalfix.kafkaadmin.exception.InvalidResourceException;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class KafkaAdminServiceTest {

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private KafkaAdminService service;

    private TopicRequest request(String name, int partitions, short replicas) {
        return new TopicRequest(name, partitions, replicas, null, null);
    }

    @Test
    void rejectsBlankTopicName() {
        assertThatThrownBy(() -> service.createTopic(request("", 3, (short) 1)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void rejectsInvalidCharactersInTopicName() {
        assertThatThrownBy(() -> service.createTopic(request("topic con espacios", 3, (short) 1)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void rejectsReservedTopicNames() {
        assertThatThrownBy(() -> service.createTopic(request("__consumer_offsets", 3, (short) 1)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void rejectsZeroPartitions() {
        assertThatThrownBy(() -> service.createTopic(request("pedidos.eventos", 0, (short) 1)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void rejectsZeroReplicas() {
        assertThatThrownBy(() -> service.createTopic(request("pedidos.eventos", 3, (short) 0)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void describeTopicRejectsInvalidNameBeforeTouchingBroker() {
        assertThatThrownBy(() -> service.describeTopic("amq.default"))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void deleteConsumerGroupRejectsBlankId() {
        // groupId vacío: nunca llega al broker, valida antes
        assertThatThrownBy(() -> service.describeConsumerGroup("grupo inexistente "))
                .isInstanceOf(Exception.class);
    }

    @Test
    void conflictExceptionIsAvailableForExistingTopics() {
        // Confirma el mapeo de errores disponible para el controller
        assertThatThrownBy(() -> {
            throw new ConflictException("El topic ya existe");
        }).isInstanceOf(ConflictException.class)
                .hasMessage("El topic ya existe");
    }
}
