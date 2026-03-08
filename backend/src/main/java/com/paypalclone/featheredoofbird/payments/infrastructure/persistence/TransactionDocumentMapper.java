package com.paypalclone.featheredoofbird.payments.infrastructure.persistence;

import com.paypalclone.featheredoofbird.payments.domain.Transaction;

public class TransactionDocumentMapper {

    public TransactionDocument toDocument(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionDocument(
                transaction.getId(),
                transaction.getSender(),
                transaction.getReceiver(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                transaction.getStatus());
    }

    public Transaction toEntity(TransactionDocument document) {
        if (document == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(document.getId());
        transaction.setSender(document.getSender());
        transaction.setReceiver(document.getReceiver());
        transaction.setAmount(document.getAmount());
        transaction.setCurrency(document.getCurrency());
        transaction.setDescription(document.getDescription());
        transaction.setCreatedAt(document.getCreatedAt());
        transaction.setUpdatedAt(document.getUpdatedAt());
        transaction.setStatus(document.getStatus());
        return transaction;
    }
}
