package com.kuchumov.springApp.entity;


import com.kuchumov.springApp.annotations.ValidType;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.Date;

@Entity
@Table(name = "data_model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class DataModel {

    @Id
    @SequenceGenerator(name = "data_model_sequence", sequenceName = "data_model_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_model_sequence")
    private Long id;
    private Date uploadDate;
    private Date changeDate;
    private String name;
    @ValidType(message = "Type valid error.")
    private String type;
    @Max(value = 15000000, message = "Size valid error. Size more than 15 MB")
    private Long size;
    private String comment;
    private String filePath;
}
