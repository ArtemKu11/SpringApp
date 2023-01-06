package com.kuchumov.springApp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataURLFileDTO {
    @ApiModelProperty(example = "privet.html")
    private String originalFilename;
    @ApiModelProperty(example = "text/html")
    private String contentType;
    @ApiModelProperty(example = "12")
    @JsonProperty("fileSize")
    private Long size;
    @ApiModelProperty(example = "0J/RgNC40LLQtdGC")
    private String dataURL;
}
