package com.example.crio.cred.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.data.Outstandings;
import com.example.crio.cred.dtos.CardAddRequestDto;
import com.example.crio.cred.dtos.CardsListDto;
import com.example.crio.cred.dtos.PayOutstandingRequestDto;
import com.example.crio.cred.dtos.PayOutstandingResponse;
import com.example.crio.cred.exceptions.CardAlreadyExistsException;
import com.example.crio.cred.exceptions.CardExpiredException;
import com.example.crio.cred.exceptions.CardNotFoundException;
import com.example.crio.cred.exceptions.InvalidOutstandingAmount;
import com.example.crio.cred.exceptions.UserNotFoundException;
import com.example.crio.cred.repository.CardRepository;
import com.example.crio.cred.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private Integer autoIncrement = 0;

    public CardEntity addCard(CardAddRequestDto requestDto) {
        CardEntity entity = cardRepository.findCardEntityByCardNumber(requestDto.getCardNumber());
        if (entity != null) {
            throw new CardAlreadyExistsException(Constants.CARD_ALREADY_EXISTS);
        }
        if (userRepository.findById(requestDto.getUserId()).isEmpty()) {
            throw new UserNotFoundException(Constants.USER_DIDNT_EXISTS);
        }
        if (Utils.differenceInDays(requestDto.getExpiryDate()) <= 0) {
            throw new CardExpiredException(Constants.CARD_EXPIRERD);
        }
        autoIncrement++;
        CardEntity cardEntity = new CardEntity(autoIncrement.toString(), requestDto.getCardNumber(),
                requestDto.getUserId(), requestDto.getExpiryDate(), requestDto.getNameOnCard(), new ArrayList<>(),
                Utils.getDateTime(), Utils.getDateTime());
        return cardRepository.save(cardEntity);

    }

    public CardsListDto getAllCards(String userid) {
        if (userRepository.findById(userid).isEmpty()) {
            throw new UserNotFoundException(Constants.USER_DIDNT_EXISTS);
        }
        List<CardEntity> cards = cardRepository.findAllCardEntitiesByUserId(userid);
        if (cards == null) {
            cards = new ArrayList<>();
        }
        return CardsListDto.builder().cards(cards).build();
    }

    public PayOutstandingResponse payCardBill(String cardId, PayOutstandingRequestDto requestDto) {
        CardEntity entity = cardRepository.findCardEntityByCardNumber(cardId);
        if (entity == null) {
            throw new CardNotFoundException(Constants.CARD_NOT_FOUND);
        }
        if (getTotalOutstanding(entity) < requestDto.getAmount()) {
            throw new InvalidOutstandingAmount(Constants.AMOUNT_EXCEEDED);
        }
        List<Outstandings> outstandingsList = entity.getOutstandings();
        Collections.sort(outstandingsList, new Comparator<Outstandings>() {
            @Override
            public int compare(Outstandings o1, Outstandings o2) {
                return o1.getDueDate().compareTo(o2.getDueDate());
            }
        });
        List<Outstandings> filteredList = new ArrayList<>();
        Double amount = requestDto.getAmount();
        for(Outstandings s : outstandingsList){
            if(amount.compareTo(0.0) == 0){
                filteredList.add(s);
                continue;
            }
            if(amount == s.getAmount()){
                amount -= s.getAmount();
            }else if(amount > s.getAmount()){
                amount -= s.getAmount();
            } else if(amount < s.getAmount()){
                filteredList.add(Outstandings.builder()
                                .amount(s.getAmount() - amount)
                                .dueDate(s.getDueDate())
                        .build());
                amount = 0.0;
            }
        }
        entity.setOutstandings(filteredList);
        entity.setUpdatedAt(Utils.getDateTime());
        cardRepository.save(entity);
        return PayOutstandingResponse.builder().cardNumber(entity.getCardNumber())
                .outstandingAmt(getTotalOutstanding(entity)).build();
    }

    public PayOutstandingResponse getTotalOutStandingAmount(String cardId){
        CardEntity entity = cardRepository.findCardEntityByCardNumber(cardId);
        if (entity == null) {
            throw new CardNotFoundException(Constants.CARD_NOT_FOUND);
        }
        return PayOutstandingResponse.builder().cardNumber(cardId).outstandingAmt(getTotalOutstanding(entity)).build();
    }

    private Double getTotalOutstanding(CardEntity entity){
        Double outstanding = 0.0;
        for(Outstandings s : entity.getOutstandings()){
            outstanding += s.getAmount();
        }
        return outstanding;
    }

}
