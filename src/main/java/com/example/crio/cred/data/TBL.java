package com.example.crio.cred.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tbl")
/**
 * id = 1 (user)
 * id = 2 (card)
 * id = 3 (transaction statement)
 */
public class TBL {
    @Id
    private String id;

    @NotNull
    private String tblId;
}

