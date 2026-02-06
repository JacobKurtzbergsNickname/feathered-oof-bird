package com.paypalclone.featheredoofbird.service;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.repository.MongoTransactionRepository;
import com.paypalclone.featheredoofbird.repository.PostgresTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class TransactionServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PostgresTransactionRepository postgresTransactionRepository;

    @Autowired
    private MongoTransactionRepository mongoTransactionRepository;

    @AfterEach
    void tearDown() {
        mongoTransactionRepository.deleteAll();
        postgresTransactionRepository.deleteAll();
    }

    @Test
    void createAndReadTransactionUsesPostgresThenMongo() {
        Transaction transaction = new Transaction();
        transaction.setSender("Frank");
        transaction.setReceiver("Gina");
        transaction.setAmount(new BigDecimal("120.00"));
        transaction.setCurrency("USD");
        transaction.setDescription("Invoice");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        Transaction saved = transactionService.createTransaction(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(postgresTransactionRepository.findById(saved.getId())).isPresent();
        assertThat(mongoTransactionRepository.findById(saved.getId())).isPresent();

        Transaction fetched = transactionService.getTransactionById(saved.getId()).orElseThrow();
        assertThat(fetched.getSender()).isEqualTo("Frank");
    }

    @Test
    void updateTransactionSyncsMongoReadModel() {
        Transaction transaction = new Transaction();
        transaction.setSender("Hugo");
        transaction.setReceiver("Iris");
        transaction.setAmount(new BigDecimal("75.00"));
        transaction.setCurrency("USD");
        transaction.setDescription("Gift");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        Transaction saved = transactionService.createTransaction(transaction);

        Transaction update = new Transaction();
        update.setSender("Hugo");
        update.setReceiver("Iris");
        update.setAmount(new BigDecimal("75.00"));
        update.setCurrency("USD");
        update.setDescription("Gift");
        update.setStatus(Transaction.TransactionStatus.COMPLETED);

        Transaction updated = transactionService.updateTransaction(saved.getId(), update);

        assertThat(updated.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        Transaction fetched = transactionService.getTransactionById(saved.getId()).orElseThrow();
        assertThat(fetched.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
    }
}
