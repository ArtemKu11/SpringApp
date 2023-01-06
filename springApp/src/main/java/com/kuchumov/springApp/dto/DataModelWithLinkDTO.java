package com.kuchumov.springApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataModelWithLinkDTO {
    private Long id;
    private Date uploadDate;
    private Date changeDate;
    private String name;
    private String type;
    private Long size;
    private String comment;
    private String downloadLink;
}
