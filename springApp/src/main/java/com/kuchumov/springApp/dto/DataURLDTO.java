package com.kuchumov.springApp.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataURLDTO {
    @ApiModelProperty(example = "dataURLcomment")
    private String comment;
    private List<DataURLFileDTO> file;
}
