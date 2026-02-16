package com.paypalclone.featheredoofbird.repository;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.model.TransactionDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoTransactionStore implements TransactionDataStore {

    private final MongoTransactionRepository mongoTransactionRepository;
    private final TransactionDocumentMapper transactionDocumentMapper = new TransactionDocumentMapper();

    @Override
    public List<Transaction> findAll() {
        return mongoTransactionRepository.findAll()
                .stream()
                .map(transactionDocumentMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return mongoTransactionRepository.findById(id)
                .map(transactionDocumentMapper::toEntity);
    }

    @Override
    public List<Transaction> findBySender(String sender) {
        return mongoTransactionRepository.findBySender(sender)
                .stream()
                .map(transactionDocumentMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByReceiver(String receiver) {
        return mongoTransactionRepository.findByReceiver(receiver)
                .stream()
                .map(transactionDocumentMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return mongoTransactionRepository.findByStatus(status)
                .stream()
                .map(transactionDocumentMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionDocument document = transactionDocumentMapper.toDocument(transaction);
        TransactionDocument savedDocument = mongoTransactionRepository.save(document);
        return transactionDocumentMapper.toEntity(savedDocument);
    }

    @Override
    public void deleteById(Long id) {
        mongoTransactionRepository.deleteById(id);
    }
}
