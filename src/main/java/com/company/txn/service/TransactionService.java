package com.company.txn.service;

import com.company.txn.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public String createTransaction() {
        return repository.insertTransaction(
                "PAYMENT",
                1000.00,
                "INR"
        );
    }
}
