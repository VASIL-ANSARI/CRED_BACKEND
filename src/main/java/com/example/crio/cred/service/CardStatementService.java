package com.example.crio.cred.service;

import java.util.ArrayList;
import java.util.List;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.data.TransactionStatement;
import com.example.crio.cred.dtos.CardStatementsListDto;
import com.example.crio.cred.dtos.StatementRequestDto;
import com.example.crio.cred.enums.TransactionCategory;
import com.example.crio.cred.exceptions.CardNotFoundException;
import com.example.crio.cred.repository.CardRepository;
import com.example.crio.cred.repository.TransactionStatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardStatementService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionStatementRepository transactionStatementRepository;

    private Integer autoIncrement = 0;

    public void addCardStatement(String cardid, String month, String year,
            StatementRequestDto requestDto) {
        CardEntity entity = cardRepository.findCardEntityByCardNumber(cardid);
        if (entity == null) {
            throw new CardNotFoundException(Constants.CARD_NOT_FOUND);
        }

        autoIncrement++;
        TransactionStatement transactionStatement = new TransactionStatement(
                autoIncrement.toString(), requestDto.getAmount(), requestDto.getVendor(),
                requestDto.getCategory(), requestDto.getMerchantCategory(), cardid, month, year);
        if (requestDto.getCategory().equals(TransactionCategory.CREDIT)) {
            entity.setOutstandingAmt(entity.getOutstandingAmt() - transactionStatement.getAmount());
        } else if (requestDto.getCategory().equals(TransactionCategory.DEBIT)) {
            entity.setOutstandingAmt(entity.getOutstandingAmt() + transactionStatement.getAmount());
        }
        entity.setUpdatedAt(Utils.getDateTime());
        cardRepository.save(entity);
        transactionStatementRepository.save(transactionStatement);
    }


    public CardStatementsListDto fetchAllCardStatements(String cardid, String month, String year) {
        List<TransactionStatement> statements =
                transactionStatementRepository.findAllTransactionStatementsByCardNumber(cardid);
        List<TransactionStatement> filteredStatements = new ArrayList<>();
        if (statements != null) {
            for (TransactionStatement st : statements) {
                if (st.getMonth().equals(month) && st.getYear().equals(year)) {
                    filteredStatements.add(st);
                }
            }
        }
        return CardStatementsListDto.builder().statements(filteredStatements).cardNumber(cardid)
                .build();
    }

}
