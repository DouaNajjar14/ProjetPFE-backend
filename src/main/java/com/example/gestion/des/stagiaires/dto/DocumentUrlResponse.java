package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUrlResponse {
    private String id;
    private String url;
    private String nomDocument;
    private String type;
    private Long tailleBytes;
    private String expiresDans;
}
