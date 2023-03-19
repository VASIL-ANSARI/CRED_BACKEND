package com.example.crio.cred.controller;

import lombok.SneakyThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.data.UserEntity;
import com.example.crio.cred.dtos.UserLoginDto;
import com.example.crio.cred.dtos.UserRequestDto;
import com.example.crio.cred.exceptions.EmailAleadyExistsException;
import com.example.crio.cred.exceptions.LoginConflictException;
import com.example.crio.cred.exceptions.PasswordMismatchException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("When User sign up with Valid Email and password")
    public void whenUserSignUpSuccess() {
        UserRequestDto dto = TestUtils.getMockUserRequestDto();
        UserEntity user = TestUtils.getMockUser();
        when(userService.addUser(dto)).thenReturn(user);
        String url = Constants.API_V1 + Constants.USER_SIGN_UP;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.userName").value(user.getUserName()))
                .andExpect(jsonPath("$.userEmail").value(user.getUserEmail()))
                .andExpect(jsonPath("$.userPassword").value(user.getUserPassword()))
                .andExpect(jsonPath("$.isLogin").value(user.getIsLogin()))
                .andExpect(jsonPath("$.createdAt").value(user.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(user.getUpdatedAt().toString()));

    }

    @Test
    @SneakyThrows
    @DisplayName("When User sign up with Invalid Email")
    public void whenUserSignUpFailure() {
        UserRequestDto dto = TestUtils.getMockUserRequestDto();
        when(userService.addUser(dto)).thenThrow(EmailAleadyExistsException.class);
        String url = Constants.API_V1 + Constants.USER_SIGN_UP;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isConflict()).andReturn();

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Login with valid Email and password")
    public void whenUserLoginSuccess() {
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        String url = Constants.API_V1 + Constants.USER_LOGIN;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        Mockito.verify(userService,times(1)).validateUser(dto);

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Login with already non-existing Email")
    public void whenUserLoginWithNonExistingEmailFailure() {
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        Mockito.doThrow(UserNotFoundException.class).when(userService).validateUser(dto);
        String url = Constants.API_V1 + Constants.USER_LOGIN;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        Mockito.verify(userService,times(1)).validateUser(dto);

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Login fail due to Password mismatch")
    public void whenUserLoginPasswordFailure() {
        UserLoginDto dto = TestUtils.getMockUserLoginDto();
        Mockito.doThrow(PasswordMismatchException.class).when(userService).validateUser(dto);
        String url = Constants.API_V1 + Constants.USER_LOGIN;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden());
        Mockito.verify(userService,times(1)).validateUser(dto);

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Logout Success")
    public void whenUserLogoutSuccess() {
        String id = "1";
        String url = Constants.API_V1 + Constants.USER_LOGOUT;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).param("id", id)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk());
        Mockito.verify(userService,times(1)).logoutUser(id);

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Logout Fail due to non-existing user")
    public void whenUserLogoutFailureForNotExistingUser() {
        String id = "1";
        Mockito.doThrow(UserNotFoundException.class).when(userService).logoutUser(id);
        String url = Constants.API_V1 + Constants.USER_LOGOUT;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).param("id", id)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        Mockito.verify(userService,times(1)).logoutUser(id);

    }

    @Test
    @SneakyThrows
    @DisplayName("When User Logout Fail due to user not logged in")
    public void whenUserLogoutFailureAsUserNotLoggedIn() {
        String id = "1";
        Mockito.doThrow(LoginConflictException.class).when(userService).logoutUser(id);
        String url = Constants.API_V1 + Constants.USER_LOGOUT;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).param("id", id)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        Mockito.verify(userService,times(1)).logoutUser(id);

    }

    @Test
    @SneakyThrows
    @DisplayName("Fetch user by id success")
    public void getUserByIdSuccess(){
        String id = "1";
        UserEntity entity = TestUtils.getMockUser();
        Mockito.when(userService.fetchUserById(id)).thenReturn(entity);
        String url = Constants.API_V1 + Constants.USER + "/" + id;
        mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(entity.getId()))
                .andExpect(jsonPath("$.userName").value(entity.getUserName()))
                .andExpect(jsonPath("$.userEmail").value(entity.getUserEmail()))
                .andExpect(jsonPath("$.userPassword").value(entity.getUserPassword()))
                .andExpect(jsonPath("$.isLogin").value(entity.getIsLogin()))
                .andExpect(jsonPath("$.createdAt").value(entity.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(entity.getUpdatedAt().toString()));
    }

    @Test
    @SneakyThrows
    @DisplayName("Fetch user by id failure as user not found")
    public void getUserByIdFailure(){
        String id = "1";
        Mockito.when(userService.fetchUserById(id)).thenThrow(UserNotFoundException.class);
        String url = Constants.API_V1 + Constants.USER + "/" + id;
        mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")).andExpect(status().isNotFound());
    }
}
