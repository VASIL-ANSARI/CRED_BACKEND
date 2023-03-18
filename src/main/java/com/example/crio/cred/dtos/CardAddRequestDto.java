package com.example.crio.cred.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.example.crio.cred.Utils.Constants;
import org.hibernate.validator.constraints.LuhnCheck;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAddRequestDto {

    @NotNull
    @LuhnCheck(startIndex = 0, endIndex = Integer.MAX_VALUE, checkDigitIndex = -1, message = Constants.INVALID_CARD)
    private String cardNumber;

    @NotNull
    private String userId;

    @NotNull
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = Constants.INVALID_EXPIRY_DATE)
    private String expiryDate;

    @NotNull
    private String nameOnCard;
    
}
