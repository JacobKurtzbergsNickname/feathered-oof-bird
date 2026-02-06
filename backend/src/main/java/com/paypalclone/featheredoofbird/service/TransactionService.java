package com.paypalclone.featheredoofbird.service;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsBySender(String sender) {
        return transactionRepository.findBySender(sender);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByReceiver(String receiver) {
        return transactionRepository.findByReceiver(receiver);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepository.update(id, updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
