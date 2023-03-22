package com.example.crio.cred.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import java.util.Optional;
import com.example.crio.cred.CredApplication;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.UserEntity;
import com.example.crio.cred.dtos.UserLoginDto;
import com.example.crio.cred.dtos.UserRequestDto;
import com.example.crio.cred.exceptions.EmailAleadyExistsException;
import com.example.crio.cred.exceptions.LoginConflictException;
import com.example.crio.cred.exceptions.PasswordMismatchException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.repository.TBLRepository;
import com.example.crio.cred.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {CredApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DirtiesContext
@ActiveProfiles("test")
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TBLRepository tblRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    @DisplayName("Add user success")
    public void addUserSuccess() {
        Mockito.when(userRepository.findUserEntityByUserEmail(anyString())).thenReturn(null);
        UserRequestDto dto = TestUtils.getMockUserRequestDto();
        UserEntity entity = TestUtils.getMockUser();
        entity.setUserPassword(Utils.encrypt(entity.getUserPassword()));
        Mockito.when(userRepository.save(any(UserEntity.class))).thenReturn(entity);
        UserEntity ent = userService.addUser(dto);
        Mockito.verify(userRepository, times(1)).save(ent);
        assertNotNull(ent);
    }

    @Test
    @DisplayName("Add user failure  as User email Already exists")
    public void addUserFailure() {
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userRepository.findUserEntityByUserEmail(anyString())).thenReturn(entity);
        UserRequestDto dto = TestUtils.getMockUserRequestDto();
        assertThrows(EmailAleadyExistsException.class, () -> {
            userService.addUser(dto);
        });
        Mockito.verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Validate user Success")
    public void validateUserSuccess() {
        UserEntity entity = TestUtils.getMockUser();
        entity.setUserPassword(Utils.encrypt(entity.getUserPassword()));
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        Mockito.when(userRepository.findUserEntityByUserEmail(anyString())).thenReturn(entity);
        entity.setIsLogin(true);
        entity.setUpdatedAt(Utils.getDateTime());
        userService.validateUser(dto);
        Mockito.verify(userRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Validate user Failure as user not found")
    public void validateUserFailureUserNotFound() {
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        Mockito.when(userRepository.findUserEntityByUserEmail(anyString())).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> {
            userService.validateUser(dto);
        });
        Mockito.verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Validate user Failure as password didn't match")
    public void validateUserFailurePasswordMismatch() {
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userRepository.findUserEntityByUserEmail(anyString())).thenReturn(entity);
        assertThrows(PasswordMismatchException.class, () -> {
            userService.validateUser(dto);
        });
        Mockito.verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("User logout success")
    public void logoutUserSuccess(){
        UserEntity entity = TestUtils.getMockUser();
        entity.setIsLogin(true);
        Mockito.when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        userService.logoutUser(entity.getId());
        entity.setIsLogin(false);
        entity.setUpdatedAt(Utils.getDateTime());
        Mockito.verify(userRepository,times(1)).save(entity);
    }

    @Test
    @DisplayName("User logout failure user not found")
    public void logoutUserFailureUserNotFound(){
        Mockito.when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userService.logoutUser("1");
        });
        Mockito.verify(userRepository,times(0)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("User logout failure user not logged in")
    public void logoutUserFailureLoginConflict(){
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userRepository.findById(anyString())).thenReturn(Optional.of(entity));
        assertThrows(LoginConflictException.class, () -> {
            userService.logoutUser(entity.getId());
        });
        Mockito.verify(userRepository,times(0)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Fetch user by id success")
    public void fetchUserByIdSuccess(){
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
        assertNotNull(userService.fetchUserById(entity.getId()));
    }

    @Test
    @DisplayName("Fetch user by id failure as user id not found")
    public void fetchUserByIdFailure(){
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userRepository.findById(entity.getId())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
           userService.fetchUserById(entity.getId());
        });
    }

}
