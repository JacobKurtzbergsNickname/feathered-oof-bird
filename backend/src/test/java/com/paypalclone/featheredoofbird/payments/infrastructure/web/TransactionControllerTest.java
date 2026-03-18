package com.paypalclone.featheredoofbird.payments.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paypalclone.featheredoofbird.payments.application.TransactionService;
import com.paypalclone.featheredoofbird.payments.domain.Transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class TransactionControllerTest {

    private MockMvc mockMvc;
    private TransactionService transactionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc =
                MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
                        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                        .build();
    }

    private Transaction sample(Long id) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setSender("alice");
        t.setReceiver("bob");
        t.setAmount(new BigDecimal("10.00"));
        t.setCurrency("USD");
        t.setDescription("test");
        t.setStatus(Transaction.TransactionStatus.PENDING);
        return t;
    }

    @Test
    void getAllTransactions_returnsOkWithList() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of(sample(1L)));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sender").value("alice"));
    }

    @Test
    void getTransactionById_returnsOkWhenFound() throws Exception {
        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(sample(1L)));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getTransactionById_returnsNotFoundWhenMissing() throws Exception {
        when(transactionService.getTransactionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/99")).andExpect(status().isNotFound());
    }

    @Test
    void getTransactionsBySender_returnsOkWithMatchingList() throws Exception {
        when(transactionService.getTransactionsBySender("alice")).thenReturn(List.of(sample(1L)));

        mockMvc.perform(get("/api/transactions/sender/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sender").value("alice"));
    }

    @Test
    void getTransactionsByReceiver_returnsOkWithMatchingList() throws Exception {
        when(transactionService.getTransactionsByReceiver("bob")).thenReturn(List.of(sample(2L)));

        mockMvc.perform(get("/api/transactions/receiver/bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiver").value("bob"));
    }

    @Test
    void getTransactionsByStatus_returnsOkWithMatchingList() throws Exception {
        when(transactionService.getTransactionsByStatus(Transaction.TransactionStatus.PENDING))
                .thenReturn(List.of(sample(1L)));

        mockMvc.perform(get("/api/transactions/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void createTransaction_returnsCreated() throws Exception {
        Transaction saved = sample(10L);
        when(transactionService.createTransaction(any())).thenReturn(saved);

        mockMvc.perform(
                        post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sample(null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateTransaction_returnsOkWhenFound() throws Exception {
        Transaction updated = sample(5L);
        when(transactionService.updateTransaction(eq(5L), any())).thenReturn(updated);

        mockMvc.perform(
                        put("/api/transactions/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sample(5L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void updateTransaction_returnsNotFoundOnException() throws Exception {
        when(transactionService.updateTransaction(eq(99L), any()))
                .thenThrow(new RuntimeException("not found"));

        mockMvc.perform(
                        put("/api/transactions/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sample(null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/1")).andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(1L);
    }
}
