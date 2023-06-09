package com.example.crio.cred.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.data.UserEntity;
import com.example.crio.cred.dtos.UserLoginDto;
import com.example.crio.cred.dtos.UserRequestDto;
import com.example.crio.cred.exceptions.EmailAleadyExistsException;
import com.example.crio.cred.exceptions.LoginConflictException;
import com.example.crio.cred.exceptions.PasswordMismatchException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(Constants.API_V1)
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(Constants.USER_SIGN_UP)
    public UserEntity signUpUser(@RequestBody @Valid UserRequestDto userRequestDto){
        try{
            return userService.addUser(userRequestDto);
        }catch(EmailAleadyExistsException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping(Constants.USER_LOGIN)
    public UserEntity loginUser(@RequestBody @Valid UserLoginDto userLoginDto){
        try {
            return userService.validateUser(userLoginDto);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch(PasswordMismatchException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping(Constants.USER_LOGOUT)
    public void logoutUser(@RequestParam @NotNull String id){
        try {
            userService.logoutUser(id);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch(LoginConflictException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(Constants.USER + "/{id}")
    public UserEntity getUser(@PathVariable("id") @NotNull String userId){
        try{
            return userService.fetchUserById(userId);
        }catch (UserNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
