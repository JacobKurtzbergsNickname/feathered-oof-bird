package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySender(String sender);
    
    List<Transaction> findByReceiver(String receiver);
    
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
