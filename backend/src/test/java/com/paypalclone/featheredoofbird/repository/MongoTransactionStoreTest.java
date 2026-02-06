package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.model.TransactionDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoTransactionStoreTest {

    private MongoTransactionRepository mongoTransactionRepository;
    private MongoTransactionStore mongoTransactionStore;

    @BeforeEach
    void setUp() {
        mongoTransactionRepository = mock(MongoTransactionRepository.class);
        mongoTransactionStore = new MongoTransactionStore(mongoTransactionRepository);
    }

    @Test
    void saveMapsTransactionToDocument() {
        Transaction transaction = sampleTransaction(2L);
        TransactionDocument savedDocument = sampleDocument(2L);
        when(mongoTransactionRepository.save(new TransactionDocumentMapper().toDocument(transaction)))
                .thenReturn(savedDocument);

        Transaction result = mongoTransactionStore.save(transaction);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getSender()).isEqualTo("dana");
        ArgumentCaptor<TransactionDocument> captor = ArgumentCaptor.forClass(TransactionDocument.class);
        verify(mongoTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
    }

    @Test
    void findByIdMapsDocumentToTransaction() {
        TransactionDocument document = sampleDocument(7L);
        when(mongoTransactionRepository.findById(7L)).thenReturn(Optional.of(document));

        Optional<Transaction> result = mongoTransactionStore.findById(7L);

        assertThat(result).isPresent();
        assertThat(result.get().getReceiver()).isEqualTo("erin");
        verify(mongoTransactionRepository).findById(7L);
    }

    private Transaction sampleTransaction(Long id) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setSender("dana");
        transaction.setReceiver("erin");
        transaction.setAmount(new BigDecimal("44.10"));
        transaction.setCurrency("USD");
        transaction.setDescription("Refund");
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        transaction.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 5));
        return transaction;
    }

    private TransactionDocument sampleDocument(Long id) {
        return new TransactionDocument(
                id,
                "dana",
                "erin",
                new BigDecimal("44.10"),
                "USD",
                "Refund",
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 1, 10, 5),
                Transaction.TransactionStatus.COMPLETED
        );
    }
}
