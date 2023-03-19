package com.example.crio.cred.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Outstandings {
    @NotNull
    private Double amount;
    @NotNull
    private LocalDate dueDate;
}
