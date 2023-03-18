package com.example.crio.cred.repository;

import java.util.List;
import com.example.crio.cred.data.TransactionStatement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionStatementRepository extends MongoRepository<TransactionStatement, String>{
    List<TransactionStatement> findAllTransactionStatementsByCardNumber(String cardNumber);
}
