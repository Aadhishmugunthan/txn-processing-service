package com.company.txn.controller;

import com.company.txn.model.TransactionRequest;
import com.company.txn.service.TransactionProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionProcessingService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn201WhenTransactionIsProcessed() throws Exception {

        TransactionRequest request = new TransactionRequest();
        request.setTxnId(UUID.randomUUID().toString());
        request.setTxnType("PAYMENT");
        request.setOperation("A");
        request.setPayload("{\"amount\":500}");

        Mockito.when(service.process(Mockito.any()))
                .thenReturn(request.getTxnId());

        mockMvc.perform(
                post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }
}
