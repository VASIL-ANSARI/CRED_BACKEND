package com.example.crio.cred.repository;

import java.util.List;
import com.example.crio.cred.data.CardEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardRepository extends MongoRepository<CardEntity, String> {
    CardEntity findCardEntityByCardNumber(String cardNumber);
    List<CardEntity> findAllCardEntitiesByUserId(String userId);
}
