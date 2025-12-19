package com.company.txn.controller;

import com.company.txn.model.TransactionRequest;
import com.company.txn.service.TransactionProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionProcessingService service;

    public TransactionController(TransactionProcessingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> process(@RequestBody TransactionRequest request) {
        String txnId = service.process(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "ACCEPTED",
                        "txnId", txnId
                ));
    }
}
