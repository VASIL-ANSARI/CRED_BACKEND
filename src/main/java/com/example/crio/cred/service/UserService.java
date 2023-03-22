package com.example.crio.cred.service;

import java.util.Optional;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.TBL;
import com.example.crio.cred.data.UserEntity;
import com.example.crio.cred.dtos.UserLoginDto;
import com.example.crio.cred.dtos.UserRequestDto;
import com.example.crio.cred.exceptions.EmailAleadyExistsException;
import com.example.crio.cred.exceptions.LoginConflictException;
import com.example.crio.cred.exceptions.PasswordMismatchException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.repository.TBLRepository;
import com.example.crio.cred.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    TBLRepository tblRepository;

    private Integer autoIncrement = 0;

    public UserEntity addUser(UserRequestDto dto) {
        UserEntity user = userRepository.findUserEntityByUserEmail(dto.getUserEmail());
        if (user != null) {
            throw new EmailAleadyExistsException(Constants.EMAIL_ALREADY_EXISTS);
        }
        Optional<TBL> tbl = tblRepository.findById("1");
        if(tbl.isEmpty()){
            autoIncrement = 0;
        }else{
            autoIncrement = Integer.parseInt(tbl.get().getTblId());
        }
        autoIncrement++;
        
        user = new UserEntity(autoIncrement.toString(), dto.getUserName(), dto.getUserEmail(),
                Utils.encrypt(dto.getUserPassword()), false, Utils.getDateTime(), Utils.getDateTime());
        tblRepository.save(new TBL("1",autoIncrement.toString()));
        return userRepository.save(user);
    }

    public UserEntity validateUser(UserLoginDto dto){
        UserEntity user = userRepository.findUserEntityByUserEmail(dto.getUserEmail());
        if(user == null){
            throw new UserNotFoundException(Constants.EMAIL_DIDNT_EXISTS);
        }
        if(Utils.decrypt(user.getUserPassword()).equals(dto.getUserPassword())){
            user.setIsLogin(true);
            user.setUpdatedAt(Utils.getDateTime());
            return userRepository.save(user);
        }else{
            throw new PasswordMismatchException(Constants.PASSWORD_MISMATCH);
        }
    }

    public void logoutUser(String userId){
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()){
            throw new UserNotFoundException(Constants.USER_DIDNT_EXISTS);
        }
        UserEntity user = userOpt.get();
        if(user.getIsLogin() == false){
            throw new LoginConflictException(Constants.USER_LOGOUT_CONFLICT);
        }
        user.setIsLogin(false);
        user.setUpdatedAt(Utils.getDateTime());
        userRepository.save(user);
    }

    public UserEntity fetchUserById(String userId){
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()){
            throw new UserNotFoundException(Constants.USER_DIDNT_EXISTS);
        }
        UserEntity user = userOpt.get();
        return user;
    }
}
