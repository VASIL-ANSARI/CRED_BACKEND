package com.example.crio.cred.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.example.crio.cred.data.CardEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardsListDto {

    @NotNull
    List<CardEntity> cards;
}
