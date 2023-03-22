package com.example.crio.cred.service;

import java.time.LocalDate;
import java.util.*;

import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.data.Outstandings;
import com.example.crio.cred.data.TBL;
import com.example.crio.cred.data.TransactionStatement;
import com.example.crio.cred.dtos.CardStatementsListDto;
import com.example.crio.cred.dtos.StatementRequestDto;
import com.example.crio.cred.enums.TransactionCategory;
import com.example.crio.cred.exceptions.CardNotFoundException;
import com.example.crio.cred.repository.CardRepository;
import com.example.crio.cred.repository.TBLRepository;
import com.example.crio.cred.repository.TransactionStatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardStatementService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionStatementRepository transactionStatementRepository;

    @Autowired
    private TBLRepository tblRepository;

    private Integer autoIncrement = 0;

    public void addCardStatement(String cardid, String month, String year,
            StatementRequestDto requestDto) {
        CardEntity entity = cardRepository.findCardEntityByCardNumber(cardid);
        if (entity == null) {
            throw new CardNotFoundException(Constants.CARD_NOT_FOUND);
        }

        Optional<TBL> tbl = tblRepository.findById("3");
        if(tbl.isEmpty()){
            autoIncrement = 0;
        }else{
            autoIncrement = Integer.parseInt(tbl.get().getTblId());
        }
        autoIncrement++;
        TransactionStatement transactionStatement = new TransactionStatement(
                autoIncrement.toString(), requestDto.getAmount(), requestDto.getVendor(),
                requestDto.getCategory(), requestDto.getMerchantCategory(), cardid, month, year);
        entity.setOutstandings(settleStatement(entity,requestDto.getCategory(), month, year, transactionStatement.getAmount()));
        entity.setUpdatedAt(Utils.getDateTime());
        tblRepository.save(new TBL("3",autoIncrement.toString()));
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

    private List<Outstandings> settleStatement(CardEntity entity, TransactionCategory category, String month, String year, Double amt){
        List<Outstandings> filteredOutStandings = new ArrayList<>();
        LocalDate date = LocalDate.of(Integer.parseInt(Utils.getYear(year)), Integer.parseInt(month), 1);
        date = date.plusMonths(1);
        Boolean found = false;
        double v = category.equals(TransactionCategory.CREDIT) ? (-1 * amt) : amt;
        for(Outstandings s: entity.getOutstandings()){
            if(s.getDueDate().getMonthValue() == date.getMonthValue() && s.getDueDate().getYear() == date.getYear()){
                found = true;
                filteredOutStandings.add(Outstandings.builder()
                                .dueDate(s.getDueDate())
                                .amount(s.getAmount() + v)
                        .build());
            }else{
                filteredOutStandings.add(s);
            }
        }
        if(!found){
            filteredOutStandings.add(Outstandings.builder()
                    .dueDate(date)
                    .amount(v)
                    .build());
        }
        Collections.sort(filteredOutStandings, new Comparator<Outstandings>() {
            @Override
            public int compare(Outstandings o1, Outstandings o2) {
                return o1.getDueDate().compareTo(o2.getDueDate());
            }
        });
        return filteredOutStandings;
    }

}
