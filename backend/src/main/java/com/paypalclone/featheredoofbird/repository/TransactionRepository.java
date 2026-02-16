package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final PostgresTransactionStore postgresTransactionStore;
    private final MongoTransactionStore mongoTransactionStore;

    public List<Transaction> findAll() {
        return mongoTransactionStore.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return mongoTransactionStore.findById(id);
    }

    public List<Transaction> findBySender(String sender) {
        return mongoTransactionStore.findBySender(sender);
    }

    public List<Transaction> findByReceiver(String receiver) {
        return mongoTransactionStore.findByReceiver(receiver);
    }

    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return mongoTransactionStore.findByStatus(status);
    }

    public Transaction save(Transaction transaction) {
        Transaction savedTransaction = postgresTransactionStore.save(transaction);
        mongoTransactionStore.save(savedTransaction);
        return savedTransaction;
    }

    public Transaction update(Long id, Transaction updatedTransaction) {
        Transaction savedTransaction = postgresTransactionStore.findById(id)
                .map(transaction -> {
                    transaction.setSender(updatedTransaction.getSender());
                    transaction.setReceiver(updatedTransaction.getReceiver());
                    transaction.setAmount(updatedTransaction.getAmount());
                    transaction.setCurrency(updatedTransaction.getCurrency());
                    transaction.setDescription(updatedTransaction.getDescription());
                    transaction.setStatus(updatedTransaction.getStatus());
                    return postgresTransactionStore.save(transaction);
                })
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        mongoTransactionStore.save(savedTransaction);
        return savedTransaction;
    }

    public void deleteById(Long id) {
        postgresTransactionStore.deleteById(id);
        mongoTransactionStore.deleteById(id);
    }
}
