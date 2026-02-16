package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostgresTransactionStore implements TransactionDataStore {

    private final PostgresTransactionRepository postgresTransactionRepository;

    @Override
    public List<Transaction> findAll() {
        return postgresTransactionRepository.findAll();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return postgresTransactionRepository.findById(id);
    }

    @Override
    public List<Transaction> findBySender(String sender) {
        return postgresTransactionRepository.findBySender(sender);
    }

    @Override
    public List<Transaction> findByReceiver(String receiver) {
        return postgresTransactionRepository.findByReceiver(receiver);
    }

    @Override
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return postgresTransactionRepository.findByStatus(status);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return postgresTransactionRepository.save(transaction);
    }

    @Override
    public void deleteById(Long id) {
        postgresTransactionRepository.deleteById(id);
    }
}
