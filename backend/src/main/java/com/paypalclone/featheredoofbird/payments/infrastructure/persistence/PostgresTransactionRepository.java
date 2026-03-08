package com.paypalclone.featheredoofbird.payments.infrastructure.persistence;

import com.paypalclone.featheredoofbird.payments.domain.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgresTransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySender(String sender);

    List<Transaction> findByReceiver(String receiver);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
