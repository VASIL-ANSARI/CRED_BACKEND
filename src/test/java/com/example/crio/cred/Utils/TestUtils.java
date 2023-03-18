package com.example.crio.cred.Utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDateTime;
import java.util.Collections;
import com.example.crio.cred.data.CardEntity;
import com.example.crio.cred.data.UserEntity;
import com.example.crio.cred.dtos.CardAddRequestDto;
import com.example.crio.cred.dtos.CardsListDto;
import com.example.crio.cred.dtos.UserLoginDto;
import com.example.crio.cred.dtos.UserRequestDto;

@UtilityClass
public class TestUtils {
    public UserEntity getMockUser() {
        return new UserEntity("1", "name", "email@gmail.com", "123@Abcd", false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public UserRequestDto getMockUserRequestDto() {
        return UserRequestDto.builder().userEmail("email@gmail.com").userName("name")
                .userPassword("123@Abcd").build();
    }

    public UserLoginDto getMockUserLoginDto() {
        return UserLoginDto.builder().userEmail("email@gmail.com").userPassword("123@Abcd").build();
    }

    public CardEntity getMockCardEntity() {
        return new CardEntity("1", "2221005440068169", "1", "12/22", "name", 0.0,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public CardAddRequestDto getMockCardAddRequestDto() {
        return CardAddRequestDto.builder().cardNumber("2221005440068169").userId("1")
                .expiryDate("12/22").nameOnCard("name").build();
    }

    public CardsListDto getMockCardsListDto(){
        return CardsListDto.builder().cards(Collections.singletonList(getMockCardEntity())).build();
    }
}
