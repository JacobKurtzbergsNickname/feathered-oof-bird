package com.paypalclone.featheredoofbird.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paypalclone.featheredoofbird.model.Transaction;
import com.paypalclone.featheredoofbird.service.TransactionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable @NonNull @NotNull Long id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sender/{sender}")
    public ResponseEntity<List<Transaction>> getTransactionsBySender(@PathVariable @NonNull @NotNull String sender) {
        return ResponseEntity.ok(transactionService.getTransactionsBySender(sender));
    }

    @GetMapping("/receiver/{receiver}")
    public ResponseEntity<List<Transaction>> getTransactionsByReceiver(@PathVariable @NonNull @NotNull String receiver) {
        return ResponseEntity.ok(transactionService.getTransactionsByReceiver(receiver));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable @NonNull @NotNull Transaction.TransactionStatus status) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_write:transactions') or hasAuthority('write:transactions')")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody @NonNull @NotNull Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write:transactions') or hasAuthority('write:transactions')")
        public ResponseEntity<Transaction> updateTransaction(
            @PathVariable @NonNull @NotNull Long id,
            @Valid @RequestBody @NonNull @NotNull Transaction transaction) {
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write:transactions') or hasAuthority('write:transactions')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable @NonNull @NotNull Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
