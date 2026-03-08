package com.paypalclone.featheredoofbird.payments.infrastructure.persistence;

import com.paypalclone.featheredoofbird.payments.domain.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionDataStore {

    List<Transaction> findAll();

    Optional<Transaction> findById(Long id);

    List<Transaction> findBySender(String sender);

    List<Transaction> findByReceiver(String receiver);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    Transaction save(Transaction transaction);

    void deleteById(Long id);
}
