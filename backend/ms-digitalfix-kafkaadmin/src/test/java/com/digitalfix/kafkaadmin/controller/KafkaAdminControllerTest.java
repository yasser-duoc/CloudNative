package com.digitalfix.kafkaadmin.controller;

import com.digitalfix.kafkaadmin.dto.TopicRequest;
import com.digitalfix.kafkaadmin.dto.TopicResponse;
import com.digitalfix.kafkaadmin.exception.GlobalExceptionHandler;
import com.digitalfix.kafkaadmin.exception.ResourceNotFoundException;
import com.digitalfix.kafkaadmin.service.KafkaAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KafkaAdminControllerTest {

    @Mock
    private KafkaAdminService kafkaAdminService;

    @InjectMocks
    private KafkaAdminController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTopicReturns201WithDetail() throws Exception {
        when(kafkaAdminService.createTopic(any())).thenReturn(
                new TopicResponse("pedidos.eventos", 3, 1, Map.of("cleanup.policy", "delete")));

        mockMvc.perform(post("/api/kafka/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TopicRequest("pedidos.eventos", 3, (short) 1, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("pedidos.eventos"))
                .andExpect(jsonPath("$.partitions").value(3));
    }

    @Test
    void createTopicRejectsBlankNameWith400() throws Exception {
        mockMvc.perform(post("/api/kafka/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TopicRequest("", 3, (short) 1, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTopicRejectsInvalidCleanupPolicyWith400() throws Exception {
        mockMvc.perform(post("/api/kafka/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TopicRequest("pedidos.eventos", 3, (short) 1, 60000L, "otra"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTopicReturns404WhenMissing() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("El topic 'x' no existe"))
                .when(kafkaAdminService).deleteTopic("x");

        mockMvc.perform(delete("/api/kafka/topics/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTopicReturns204() throws Exception {
        mockMvc.perform(delete("/api/kafka/topics/pedidos.eventos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listConsumerGroupsReturns200() throws Exception {
        when(kafkaAdminService.listConsumerGroups()).thenReturn(java.util.List.of("ms-digitalfix-audit"));

        mockMvc.perform(get("/api/kafka/consumer-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ms-digitalfix-audit"));
    }
}
