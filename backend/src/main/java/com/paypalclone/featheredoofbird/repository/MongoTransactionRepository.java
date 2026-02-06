package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.model.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoTransactionRepository extends MongoRepository<TransactionDocument, Long> {

    List<TransactionDocument> findBySender(String sender);

    List<TransactionDocument> findByReceiver(String receiver);

    List<TransactionDocument> findByStatus(Transaction.TransactionStatus status);
}
