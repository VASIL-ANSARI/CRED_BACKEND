package com.example.crio.cred.service;

import java.util.ArrayList;
import java.util.List;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Utils;
import com.example.crio.cred.data.CardEntity;
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
                requestDto.getUserId(), requestDto.getExpiryDate(), requestDto.getNameOnCard(), 0.0,
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
        if (entity.getOutstandingAmt() < requestDto.getAmount()) {
            throw new InvalidOutstandingAmount(Constants.INVALID_AMOUNT);
        }
        entity.setOutstandingAmt(entity.getOutstandingAmt() - requestDto.getAmount());
        entity.setUpdatedAt(Utils.getDateTime());
        cardRepository.save(entity);
        return PayOutstandingResponse.builder().cardNumber(entity.getCardNumber())
                .outstandingAmt(entity.getOutstandingAmt()).build();
    }

}
