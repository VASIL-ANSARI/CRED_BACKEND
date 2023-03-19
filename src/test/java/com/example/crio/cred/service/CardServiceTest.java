package com.example.crio.cred.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.example.crio.cred.CredApplication;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.dtos.CardAddRequestDto;
import com.example.crio.cred.dtos.CardsListDto;
import com.example.crio.cred.exceptions.CardAlreadyExistsException;
import com.example.crio.cred.exceptions.CardExpiredException;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.repository.CardRepository;
import com.example.crio.cred.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
public class CardServiceTest {
    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Add Card Success")
    public void  addCardSuccess(){
        CardEntity entity = TestUtils.getMockCardEntity();
        entity.setExpiryDate("12/26");
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        dto.setExpiryDate("12/26");
        Mockito.when(cardRepository.findCardEntityByCardNumber(entity.getCardNumber())).thenReturn(null);
        Mockito.when(userRepository.findById(entity.getUserId())).thenReturn(Optional.of(TestUtils.getMockUser()));
        Mockito.when(cardRepository.save(entity)).thenReturn(entity);
        CardEntity ent = cardService.addCard(dto);
        Mockito.verify(cardRepository,times(1)).save(ent);
        assertNotNull(ent);

    }

    @Test
    @DisplayName("Add Card Failure as card already exists")
    public void  addCardFailureCardAlreadyExists(){
        CardEntity entity = TestUtils.getMockCardEntity();
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardRepository.findCardEntityByCardNumber(entity.getCardNumber())).thenReturn(entity);
        assertThrows(CardAlreadyExistsException.class, () -> {
            cardService.addCard(dto);
        });
        Mockito.verify(cardRepository,times(0)).save(any(CardEntity.class));

    }

    @Test
    @DisplayName("Add Card Failure as user didn't exist")
    public void  addCardFailureUserDidntExist(){
        CardEntity entity = TestUtils.getMockCardEntity();
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardRepository.findCardEntityByCardNumber(entity.getCardNumber())).thenReturn(null);
        Mockito.when(userRepository.findById(entity.getUserId())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            cardService.addCard(dto);
        });
        Mockito.verify(cardRepository,times(0)).save(any(CardEntity.class));

    }

    @Test
    @DisplayName("Add Card Failure as card already expired")
    public void  addCardFailureCardAlreadyExpired(){
        CardEntity entity = TestUtils.getMockCardEntity();
        CardAddRequestDto dto = TestUtils.getMockCardAddRequestDto();
        Mockito.when(cardRepository.findCardEntityByCardNumber(entity.getCardNumber())).thenReturn(null);
        Mockito.when(userRepository.findById(entity.getUserId())).thenReturn(Optional.of(TestUtils.getMockUser()));
        assertThrows(CardExpiredException.class, () -> {
            cardService.addCard(dto);
        });
        Mockito.verify(cardRepository,times(0)).save(any(CardEntity.class));

    }

    @Test
    @DisplayName("Get All cards success")
    public void getAllCardsSuccess(){
        List<CardEntity> cards = Collections.singletonList(TestUtils.getMockCardEntity());
        Mockito.when(cardRepository.findAllCardEntitiesByUserId(anyString())).thenReturn(cards);
        Mockito.when(userRepository.findById(anyString())).thenReturn(Optional.of(TestUtils.getMockUser()));
        CardsListDto dto = cardService.getAllCards("1");
        assertNotNull(dto);
        assertEquals(dto.getCards().size(), cards.size());
    }

    @Test
    @DisplayName("Get All cards failure as user not found")
    public void getAllCardsFailureAsUserNotFound(){
        Mockito.when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            cardService.getAllCards("1");
        });
        Mockito.verify(cardRepository,times(0)).findAllCardEntitiesByUserId(anyString());
    }
}
