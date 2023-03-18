package com.example.crio.cred.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.data.TransactionStatement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardStatementsListDto {
    @NotNull
    private String cardNumber;
    @NotNull
    private List<TransactionStatement> statements;
}
