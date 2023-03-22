package com.example.crio.cred.repository;

import com.example.crio.cred.data.TBL;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TBLRepository extends MongoRepository<TBL,String> {
}
