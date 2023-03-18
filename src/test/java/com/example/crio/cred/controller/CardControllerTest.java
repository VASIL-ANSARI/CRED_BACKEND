package com.example.crio.cred.controller;

import lombok.SneakyThrows;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.dtos.CardAddRequestDto;
import com.example.crio.cred.dtos.CardsListDto;
import com.example.crio.cred.exceptions.CardAlreadyExistsException;
import com.example.crio.cred.exceptions.CardExpiredException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.service.CardService;
import com.example.crio.cred.service.CardStatementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class CardControllerTest {

    @MockBean
    private CardService cardService;

    @MockBean
    private CardStatementService cardStatementService;

    @InjectMocks
    private CardController cardController;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }
    
    @Test
    @SneakyThrows
    @DisplayName("When new Card is added successfully")
    public void whenAddCardSuccess() {
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        CardEntity card = TestUtils.getMockCardEntity();
        Mockito.when(cardService.addCard(dto)).thenReturn(card);
        String url = Constants.API_V1 + Constants.CARD;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.cardNumber").value(card.getCardNumber()))
                .andExpect(jsonPath("$.nameOnCard").value(card.getNameOnCard()))
                .andExpect(jsonPath("$.userId").value(card.getUserId()))
                .andExpect(jsonPath("$.expiryDate").value(card.getExpiryDate()))
                .andExpect(jsonPath("$.outstandingAmt").value(card.getOutstandingAmt()))
                .andExpect(jsonPath("$.createdAt").value(card.getCreatedAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(card.getUpdatedAt().toString()));
    }

    @Test
    @SneakyThrows
    @DisplayName("When Card already exists failure")
    public void whenCardAlreadyExistsFailure() {
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardService.addCard(dto)).thenThrow(CardAlreadyExistsException.class);
        String url = Constants.API_V1 + Constants.CARD;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When User not found failure")
    public void whenCardHolderNotFoundFailure() {
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardService.addCard(dto)).thenThrow(UserNotFoundException.class);
        String url = Constants.API_V1 + Constants.CARD;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When Card already expired failure")
    public void whenCardAlreadyExpiredFailure() {
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardService.addCard(dto)).thenThrow(CardExpiredException.class);
        String url = Constants.API_V1 + Constants.CARD;
        String json = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When all card details successfully fetched")
    public void whenGetAllCardDetailsSuccess() {
        CardsListDto dto = TestUtils.getMockCardsListDto();
        String userId = "1";
        Mockito.when(cardService.getAllCards(userId)).thenReturn(dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + userId;
        MvcResult result = mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
        CardsListDto response = objectMapper.readValue(result.getResponse().getContentAsString(), CardsListDto.class);
        assertEquals(response.getCards().size(), dto.getCards().size());
    }

    @Test
    @SneakyThrows
    @DisplayName("When all card details can not be fetched as user not found")
    public void whenGetAllCardDetailsFetchFailure() {
        String userId = "1";
        Mockito.when(cardService.getAllCards(userId)).thenThrow(UserNotFoundException.class);
        String url = Constants.API_V1 + Constants.CARD + "/" + userId;
        mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }


}
