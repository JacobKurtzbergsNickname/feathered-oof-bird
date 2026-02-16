package com.paypalclone.featheredoofbird.service;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.repository.TransactionRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(@NonNull @NotNull Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsBySender(@NonNull @NotNull String sender) {
        return transactionRepository.findBySender(sender);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByReceiver(@NonNull @NotNull String receiver) {
        return transactionRepository.findByReceiver(receiver);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByStatus(@NonNull @NotNull Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    @Transactional
    public Transaction createTransaction(@NonNull @NotNull Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(@NonNull @NotNull Long id, @NonNull @NotNull Transaction updatedTransaction) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        transaction.setSender(updatedTransaction.getSender());
        transaction.setReceiver(updatedTransaction.getReceiver());
        transaction.setAmount(updatedTransaction.getAmount());
        transaction.setCurrency(updatedTransaction.getCurrency());
        transaction.setDescription(updatedTransaction.getDescription());
        transaction.setStatus(updatedTransaction.getStatus());
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(@NonNull @NotNull Long id) {
        transactionRepository.deleteById(id);
    }
}
