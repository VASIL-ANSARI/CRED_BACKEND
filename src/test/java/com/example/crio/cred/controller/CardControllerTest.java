package com.example.crio.cred.controller;

import lombok.SneakyThrows;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.dtos.CardAddRequestDto;
import com.example.crio.cred.dtos.CardStatementsListDto;
import com.example.crio.cred.dtos.CardsListDto;
import com.example.crio.cred.dtos.PayOutstandingRequestDto;
import com.example.crio.cred.dtos.PayOutstandingResponse;
import com.example.crio.cred.dtos.StatementRequestDto;
import com.example.crio.cred.exceptions.CardAlreadyExistsException;
import com.example.crio.cred.exceptions.CardExpiredException;
import com.example.crio.cred.exceptions.CardNotFoundException;
import com.example.crio.cred.exceptions.InvalidOutstandingAmount;
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
import static org.mockito.Mockito.times;
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


    @Test
    @SneakyThrows
    @DisplayName("When card statement added successfully")
    public void whenAddCardStatementSuccess() {
        String cardId = "2221005440068169";
        String year = "22";
        String month = "01";
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        String json = objectMapper.writeValueAsString(dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @DisplayName("When card statement addition fail as amount is not valid")
    public void whenAddCardStatementFailureAmountNotvalid() {
        String cardId = "2221005440068169";
        String year = "22";
        String month = "01";
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        dto.setAmount(0.0);
        String json = objectMapper.writeValueAsString(dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When card statement addition fail as year and month is not valid")
    public void whenAddCardStatementFailureYearMonthtNotvalid() {
        String cardId = "2221005440068169";
        String year = "26";
        String month = "01";
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        String json = objectMapper.writeValueAsString(dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @SneakyThrows
    @DisplayName("When card statement addition failure as card not found")
    public void whenAddCardStatementFailureCardNotFound() {
        String cardId = "2221005440068169";
        String year = "22";
        String month = "01";
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        String json = objectMapper.writeValueAsString(dto);
        Mockito.doThrow(CardNotFoundException.class).when(cardStatementService).addCardStatement(cardId, month, year, dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When card statement fetched successfully")
    public void whenCardStatementFetchedSuccess() {
        String cardId = "2221005440068169";
        String year = "22";
        String month = "01";
        CardStatementsListDto dto = TestUtils.getMockcCardStatementsListDto();
        String json = objectMapper.writeValueAsString(dto);
        Mockito.when(cardStatementService.fetchAllCardStatements(cardId, month, year)).thenReturn(dto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        MvcResult result = mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        assertEquals(result.getResponse().getContentAsString(), json);
    }


    @Test
    @SneakyThrows
    @DisplayName("When card statement fetched fail as year and month is not valid")
    public void whenCardStatementFetchFailureYearMonthtNotvalid() {
        String cardId = "2221005440068169";
        String year = "26";
        String month = "01";
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.STATEMENTS + "/" + year + "/" + month;
        mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @SneakyThrows
    @DisplayName("When card bill paid successfully")
    public void whenCardBillPaySuccess() {
        String cardId = "2221005440068169";
        PayOutstandingRequestDto requestDto = TestUtils.getMockPayOutstandingRequestDto();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        PayOutstandingResponse responseDto = TestUtils.getMockPayOutstandingResponse();
        String responseJson = objectMapper.writeValueAsString(responseDto);

        Mockito.when(cardService.payCardBill(cardId, requestDto)).thenReturn(responseDto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.PAY_API;
        MvcResult result = mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        assertEquals(result.getResponse().getContentAsString(), responseJson);
    }

    @Test
    @SneakyThrows
    @DisplayName("When card bill paid failure as amount is not valid")
    public void whenCardBillPayFailureAmountNotValid() {
        String cardId = "2221005440068169";
        PayOutstandingRequestDto requestDto = TestUtils.getMockPayOutstandingRequestDto();
        requestDto.setAmount(0.0);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.PAY_API;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
        Mockito.verify(cardService, times(0)).payCardBill(cardId, requestDto);
    }

    @Test
    @SneakyThrows
    @DisplayName("When card bill paid failure as card is not found")
    public void whenCardBillPayFailureCardNotFound() {
        String cardId = "2221005440068169";
        PayOutstandingRequestDto requestDto = TestUtils.getMockPayOutstandingRequestDto();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        Mockito.when(cardService.payCardBill(cardId, requestDto)).thenThrow(CardNotFoundException.class);

        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.PAY_API;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .characterEncoding("utf-8"))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("When card bill paid failure as outstanding balance is not valid")
    public void whenCardBillPayFailureBalanceNotValid() {
        String cardId = "2221005440068169";
        PayOutstandingRequestDto requestDto = TestUtils.getMockPayOutstandingRequestDto();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        Mockito.when(cardService.payCardBill(cardId, requestDto)).thenThrow(InvalidOutstandingAmount.class);

        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.PAY_API;
        mockMvc
                .perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("When get card bill success")
    public void whenGetCardBillSuccess() {
        String cardId = "2221005440068169";

        PayOutstandingResponse responseDto = TestUtils.getMockPayOutstandingResponse();
        String responseJson = objectMapper.writeValueAsString(responseDto);

        Mockito.when(cardService.getTotalOutStandingAmount(cardId)).thenReturn(responseDto);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.BILL;
        MvcResult result = mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        assertEquals(result.getResponse().getContentAsString(), responseJson);
    }

    @Test
    @SneakyThrows
    @DisplayName("When get card bill failure as card not found")
    public void whenGetCardBillFailure() {
        String cardId = "2221005440068169";

        Mockito.when(cardService.getTotalOutStandingAmount(cardId)).thenThrow(CardNotFoundException.class);
        String url = Constants.API_V1 + Constants.CARD + "/" + cardId + Constants.BILL;
        mockMvc
                .perform(get(url).contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNotFound());
    }

}
