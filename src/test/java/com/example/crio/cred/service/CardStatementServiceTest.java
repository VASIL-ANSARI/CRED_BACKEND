package com.example.crio.cred.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import com.example.crio.cred.CredApplication;
import com.example.crio.cred.Utils.TestUtils;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.data.Outstandings;
import com.example.crio.cred.data.TransactionStatement;
import com.example.crio.cred.dtos.CardStatementsListDto;
import com.example.crio.cred.dtos.StatementRequestDto;
import com.example.crio.cred.enums.TransactionCategory;
import com.example.crio.cred.exceptions.CardNotFoundException;
import com.example.crio.cred.repository.CardRepository;
import com.example.crio.cred.repository.TransactionStatementRepository;
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
public class CardStatementServiceTest {

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private TransactionStatementRepository transactionStatementRepository;

    @InjectMocks
    private CardStatementService statementService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Add card statement success")
    public void addCardStatementSuccessForDebit() {
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        TransactionStatement transactionStatement = TestUtils.getMockTransactionStatement();
        Outstandings outstandings = TestUtils.getMockOutstandings(12.0);
        outstandings.setDueDate(LocalDate.of(2023,1,1));

        String cardid = cardEntity.getCardNumber();
        String month = "12";
        String year = "22";

        Mockito.when(cardRepository.findCardEntityByCardNumber(cardEntity.getCardNumber()))
                .thenReturn(cardEntity);
        outstandings.setAmount(outstandings.getAmount() + dto.getAmount());
        cardEntity.setOutstandings(Collections.singletonList(outstandings));
        cardEntity.setUpdatedAt(Utils.getDateTime());

        statementService.addCardStatement(cardid, month, year, dto);

        Mockito.verify(cardRepository, times(1)).save(cardEntity);
        Mockito.verify(transactionStatementRepository, times(1)).save(transactionStatement);

    }

    @Test
    @DisplayName("Add card statement success")
    public void addCardStatementSuccess() {
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        TransactionStatement transactionStatement = TestUtils.getMockTransactionStatement();
        Outstandings outstandings = TestUtils.getMockOutstandings(12.0);
        cardEntity.setOutstandings(Collections.singletonList(outstandings));

        String cardid = cardEntity.getCardNumber();
        String month = "12";
        String year = "22";

        Mockito.when(cardRepository.findCardEntityByCardNumber(cardEntity.getCardNumber()))
                .thenReturn(cardEntity);
        outstandings.setAmount(outstandings.getAmount() + dto.getAmount());
        cardEntity.setOutstandings(Collections.singletonList(outstandings));
        cardEntity.setUpdatedAt(Utils.getDateTime());

        statementService.addCardStatement(cardid, month, year, dto);

        Mockito.verify(cardRepository, times(1)).save(cardEntity);
        Mockito.verify(transactionStatementRepository, times(1)).save(transactionStatement);

    }

    @Test
    @DisplayName("Add card statement success")
    public void addCardStatementSuccessForCredit() {
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();
        dto.setCategory(TransactionCategory.CREDIT);
        TransactionStatement transactionStatement = TestUtils.getMockTransactionStatement();
        transactionStatement.setCategory(TransactionCategory.CREDIT);
        Outstandings outstandings = TestUtils.getMockOutstandings(100.0);

        String cardid = cardEntity.getCardNumber();
        String month = "12";
        String year = "22";

        Mockito.when(cardRepository.findCardEntityByCardNumber(cardEntity.getCardNumber()))
                .thenReturn(cardEntity);
        outstandings.setAmount(outstandings.getAmount() - dto.getAmount());
        cardEntity.setOutstandings(Collections.singletonList(outstandings));
        cardEntity.setUpdatedAt(Utils.getDateTime());

        statementService.addCardStatement(cardid, month, year, dto);

        Mockito.verify(cardRepository, times(1)).save(cardEntity);
        Mockito.verify(transactionStatementRepository, times(1)).save(transactionStatement);

    }

    @Test
    @DisplayName("Add card statement failure as card not found")
    public void addCardStatementFailure() {
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        StatementRequestDto dto = TestUtils.getMockStatementRequestDto();

        String cardid = cardEntity.getCardNumber();
        String month = "12";
        String year = "22";

        Mockito.when(cardRepository.findCardEntityByCardNumber(cardEntity.getCardNumber()))
                .thenReturn(null);

        assertThrows(CardNotFoundException.class, () -> {
            statementService.addCardStatement(cardid, month, year, dto);
        });


        Mockito.verify(cardRepository, times(0)).save(any(CardEntity.class));
        Mockito.verify(transactionStatementRepository, times(0))
                .save(any(TransactionStatement.class));

    }

    @Test
    @DisplayName("Fetch all card statements")
    public void fetchAllCardStatementsTest(){
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        String cardid = cardEntity.getCardNumber();
        String month = "12";
        String year = "22";
        TransactionStatement transactionStatement = TestUtils.getMockTransactionStatement();
        List<TransactionStatement> statements = Collections.singletonList(transactionStatement);
        Mockito.when(transactionStatementRepository.findAllTransactionStatementsByCardNumber(cardid)).thenReturn(statements);
        CardStatementsListDto result = statementService.fetchAllCardStatements(cardid, month, year);
        assertEquals(result.getStatements().size(), statements.size());
    }

    @Test
    @DisplayName("Fetch all card statements")
    public void fetchAllCardStatementsTestNoStatement(){
        CardEntity cardEntity = TestUtils.getMockCardEntity();
        String cardid = cardEntity.getCardNumber();
        String month = "02";
        String year = "22";
        TransactionStatement transactionStatement = TestUtils.getMockTransactionStatement();
        List<TransactionStatement> statements = Collections.singletonList(transactionStatement);
        Mockito.when(transactionStatementRepository.findAllTransactionStatementsByCardNumber(cardid)).thenReturn(statements);
        CardStatementsListDto result = statementService.fetchAllCardStatements(cardid, month, year);
        assertEquals(result.getStatements().size(), 0);
    }
}
