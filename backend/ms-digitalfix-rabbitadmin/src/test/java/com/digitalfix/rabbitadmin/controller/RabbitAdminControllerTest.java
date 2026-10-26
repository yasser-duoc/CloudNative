package com.digitalfix.rabbitadmin.controller;

import com.digitalfix.rabbitadmin.dto.QueueRequest;
import com.digitalfix.rabbitadmin.service.RabbitAdminService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RabbitAdminControllerTest {

    @Mock
    private RabbitAdminService rabbitAdminService;

    @InjectMocks
    private RabbitAdminController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createQueueReturns201() throws Exception {
        when(rabbitAdminService.createQueue(any()))
                .thenReturn(org.springframework.amqp.core.QueueBuilder.durable("pedidos.nueva").build());

        mockMvc.perform(post("/api/rabbit/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QueueRequest("pedidos.nueva", true, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("pedidos.nueva"));
    }

    @Test
    void createQueueRejectsBlankNameWith400() throws Exception {
        mockMvc.perform(post("/api/rabbit/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QueueRequest("", true, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQueueRejectsInvalidCharactersWith400() throws Exception {
        mockMvc.perform(post("/api/rabbit/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QueueRequest("cola con espacios", true, null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteQueueReturns204() throws Exception {
        mockMvc.perform(delete("/api/rabbit/queues/pedidos.nueva"))
                .andExpect(status().isNoContent());
    }
}
