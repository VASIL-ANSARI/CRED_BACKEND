package com.example.crio.cred.repository;

import com.example.crio.cred.data.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {
    UserEntity findUserEntityByUserEmail(String userEmail);
}
