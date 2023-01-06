package com.kuchumov.springApp.utilites.mappers;

import com.kuchumov.springApp.dto.DataModelWithLinkDTO;
import com.kuchumov.springApp.dto.DataURLFileDTO;
import com.kuchumov.springApp.entity.DataModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper
public interface DataModelMapper {
    DataModelMapper MAPPER = Mappers.getMapper(DataModelMapper.class);

    @Mapping(target = "uploadDate", expression = "java(new java.util.Date())")
    @Mapping(target = "changeDate", expression = "java(new java.util.Date())")
    @Mapping(target = "name", source = "dataURLFileDTO.originalFilename")
    @Mapping(target = "type", source = "dataURLFileDTO.contentType")
    @Mapping(target = "size", source = "dataURLFileDTO.size")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "filePath", source = "filePath")
    DataModel dataURLFileToModel(DataURLFileDTO dataURLFileDTO, String comment, String filePath);


    @Mapping(target = "uploadDate", expression = "java(new java.util.Date())")
    @Mapping(target = "changeDate", expression = "java(new java.util.Date())")
    @Mapping(target = "name", source = "multipartFile.originalFilename")
    @Mapping(target = "type", source = "multipartFile.contentType")
    @Mapping(target = "size", source = "multipartFile.size")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "filePath", source = "filePath")
    DataModel formDataFileToModel(MultipartFile multipartFile, String comment, String filePath) throws IOException;

    @Mapping(target = "downloadLink", source = "link")
    DataModelWithLinkDTO modelToDataModelWithLinkDTO(DataModel dataModel, String link);

}
