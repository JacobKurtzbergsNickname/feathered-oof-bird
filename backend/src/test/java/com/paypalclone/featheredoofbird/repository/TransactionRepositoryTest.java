package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionRepositoryTest {

    private PostgresTransactionStore postgresTransactionStore;
    private MongoTransactionStore mongoTransactionStore;
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        postgresTransactionStore = mock(PostgresTransactionStore.class);
        mongoTransactionStore = mock(MongoTransactionStore.class);
        transactionRepository = new TransactionRepository(postgresTransactionStore, mongoTransactionStore);
    }

    @Test
    void findAllUsesMongoStore() {
        Transaction transaction = sampleTransaction(1L);
        when(mongoTransactionStore.findAll()).thenReturn(List.of(transaction));

        List<Transaction> result = transactionRepository.findAll();

        assertThat(result).containsExactly(transaction);
        verify(mongoTransactionStore).findAll();
    }

    @Test
    void saveWritesToPostgresAndSyncsMongo() {
        Transaction transaction = sampleTransaction(null);
        Transaction savedTransaction = sampleTransaction(10L);
        when(postgresTransactionStore.save(transaction)).thenReturn(savedTransaction);
        when(mongoTransactionStore.save(savedTransaction)).thenReturn(savedTransaction);

        Transaction result = transactionRepository.save(transaction);

        assertThat(result).isEqualTo(savedTransaction);
        verify(postgresTransactionStore).save(transaction);
        verify(mongoTransactionStore).save(savedTransaction);
    }

    @Test
    void updateWritesToPostgresAndSyncsMongo() {
        Transaction existing = sampleTransaction(5L);
        Transaction update = sampleTransaction(5L);
        update.setStatus(Transaction.TransactionStatus.COMPLETED);
        Transaction updated = sampleTransaction(5L);
        updated.setStatus(Transaction.TransactionStatus.COMPLETED);

        when(postgresTransactionStore.findById(5L)).thenReturn(Optional.of(existing));
        when(postgresTransactionStore.save(existing)).thenReturn(updated);

        Transaction result = transactionRepository.update(5L, update);

        assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(mongoTransactionStore).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
    }

    @Test
    void deleteRemovesFromBothStores() {
        transactionRepository.deleteById(9L);

        verify(postgresTransactionStore).deleteById(9L);
        verify(mongoTransactionStore).deleteById(9L);
    }

    private Transaction sampleTransaction(Long id) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setSender("alice");
        transaction.setReceiver("bob");
        transaction.setAmount(new BigDecimal("12.34"));
        transaction.setCurrency("USD");
        transaction.setDescription("Dinner");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        return transaction;
    }
}
