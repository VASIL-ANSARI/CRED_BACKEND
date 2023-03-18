package com.example.crio.cred.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.enums.TransactionCategory;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transaction_statement")
public class TransactionStatement {

    @Id
    private String id;

    @NotNull
    private Double amount;

    @NotNull
    @NotBlank
    private String vendor;

    @NotNull
    private TransactionCategory category;

    @NotBlank
    @NotNull
    private String merchantCategory;

    @NotNull
    @LuhnCheck(startIndex = 0, endIndex = Integer.MAX_VALUE, checkDigitIndex = -1)
    private String cardNumber;

    @NotNull
    private String month;

    @NotNull
    private String year;

    
    
}
