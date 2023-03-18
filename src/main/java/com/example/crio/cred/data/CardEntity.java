package com.example.crio.cred.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.example.crio.cred.Utils.Constants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "card")
public class CardEntity {
    @Id
    private String id;

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

    @NotNull
    private Double outstandingAmt = 0.0;

    @NotNull
    @CreationTimestamp
    private LocalDateTime createdAt;

    @NotNull
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
