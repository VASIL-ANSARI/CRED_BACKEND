package com.example.crio.cred.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.enums.TransactionCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementRequestDto {

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
}
