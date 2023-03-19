package com.example.crio.cred.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.Utils.Constants;
import com.example.crio.cred.Utils.Validation;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(Constants.API_V1)
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardStatementService cardStatementService;

    @PostMapping(Constants.CARD)
    public CardEntity addCreditCard(@RequestBody @Valid CardAddRequestDto requestDto) {
        try {
            return cardService.addCard(requestDto);
        } catch (CardAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CardExpiredException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(Constants.CARD + "/{userid}")
    public CardsListDto fetchAllCards(@PathVariable("userid") @NotNull String userid) {
        try {
            return cardService.getAllCards(userid);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(Constants.CARD + "/{id}" + Constants.STATEMENTS + "/{year}/{month}")
    public void addStatement(@PathVariable("id") @NotNull String cardId,
            @PathVariable("year") @NotNull String year,
            @PathVariable("month") @NotNull String month,
            @RequestBody @Valid StatementRequestDto requestDto) {
        if (Validation.validateMonthAndYear(month, year)
                && Validation.validateAmount(requestDto.getAmount())) {
            try {
                cardStatementService.addCardStatement(cardId, month, year, requestDto);
            } catch (CardNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.MONTH_YEAR_INVALID);
        }
    }

    @GetMapping(Constants.CARD + "/{id}" + Constants.STATEMENTS + "/{year}/{month}")
    public CardStatementsListDto fetchStatement(@PathVariable("id") @NotNull String cardId,
            @PathVariable("year") @NotNull String year,
            @PathVariable("month") @NotNull String month) {
        if (Validation.validateMonthAndYear(month, year)) {
            return cardStatementService.fetchAllCardStatements(cardId, month, year);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.MONTH_YEAR_INVALID);
        }
    }

    @GetMapping(Constants.CARD + "/{id}" + Constants.BILL)
    public PayOutstandingResponse getCardBill(@PathVariable("id") @NotNull String cardId){
        try{
            return cardService.getTotalOutStandingAmount(cardId);
        }catch(CardNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping(Constants.CARD + "/{id}" + Constants.PAY_API)
    public PayOutstandingResponse payCardBill(@PathVariable("id") @NotNull String cardId,
            @RequestBody @Valid PayOutstandingRequestDto requestDto) {
        if (Validation.validateAmount(requestDto.getAmount())) {
            try {
                return cardService.payCardBill(cardId, requestDto);
            } catch (CardNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (InvalidOutstandingAmount e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_AMOUNT);
        }
    }
}
