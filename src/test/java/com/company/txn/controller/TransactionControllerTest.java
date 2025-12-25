package com.company.txn.controller;

import com.company.txn.service.TransactionProcessingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionProcessingService service;

    @Test
    void shouldReturn201WhenTransactionIsProcessed() throws Exception {
        Mockito.when(service.process(any())).thenReturn("txn-123");

        String requestJson = """
            {
              "txnType": "PAYMENT",
              "payload": "{\\"amount\\":100,\\"currency\\":\\"INR\\"}"
            }
            """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.txnId").value("txn-123"));
    }

    @Test
    void shouldProcessRefundTransaction() throws Exception {
        Mockito.when(service.process(any())).thenReturn("txn-refund-456");

        String requestJson = """
            {
              "txnType": "REFUND",
              "payload": "{\\"amount\\":50}"
            }
            """;

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.txnId").value("txn-refund-456"));
    }

    @Test
    void shouldProcessMultipleTransactions() throws Exception {
        Mockito.when(service.process(any()))
                .thenReturn("txn-1")
                .thenReturn("txn-2");

        String request1 = """
            {
              "txnType": "PAYMENT",
              "payload": "{\\"amount\\":100}"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request1))
                .andExpect(status().isCreated());
    }
}