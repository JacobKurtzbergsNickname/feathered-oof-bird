package com.paypalclone.featheredoofbird.payments.infrastructure.persistence;

import com.paypalclone.featheredoofbird.payments.domain.Transaction;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoTransactionRepository extends MongoRepository<TransactionDocument, Long> {

    List<TransactionDocument> findBySender(String sender);

    List<TransactionDocument> findByReceiver(String receiver);

    List<TransactionDocument> findByStatus(Transaction.TransactionStatus status);
}
