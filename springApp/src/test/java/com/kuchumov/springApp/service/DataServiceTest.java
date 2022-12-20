package com.kuchumov.springApp.service;

import com.kuchumov.springApp.DTO.DataURLFileDTO;
import com.kuchumov.springApp.DTO.EmptyResponseDTO;
import com.kuchumov.springApp.DTO.NewDataURLDTO;
import com.kuchumov.springApp.DTO.NewFormDataDTO;
import com.kuchumov.springApp.entity.DataModel;
import com.kuchumov.springApp.exceptionHandler.customExceptions.FileIdNotFoundException;
import com.kuchumov.springApp.exceptionHandler.customExceptions.ParsingDateException;
import com.kuchumov.springApp.repository.DataModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
class DataServiceTest {

    @InjectMocks
    DataService dataService;

    @Mock
    DataModelRepository dataModelRepository;


    @Test
    void saveDataURLFilesTest() { // + вложенный protected mapDataURLDTOToModel
        List <DataURLFileDTO> dataURLFileDTO = List.of( // Данные на вход
                DataURLFileDTO.builder()
                        .dataURL("0J/RgNC40LLQtdGC")
                        .contentType("text/html")
                        .originalFilename("privet.html")
                        .size(12L)
                        .build()
        );
        NewDataURLDTO newDataURLDTO = new NewDataURLDTO("dataURLComment", dataURLFileDTO);

        String dataURL = dataURLFileDTO.get(0).getDataURL();
        byte[] decodedBytes = Base64.getDecoder().decode(dataURL);

        List<DataModel> dataModelList = List.of( // Для Mockito
                DataModel.builder()
                        .file(decodedBytes)
                        .name(newDataURLDTO.getFile().get(0).getOriginalFilename())
                        .type(newDataURLDTO.getFile().get(0).getContentType())
                        .comment(newDataURLDTO.getComment())
                        .uploadDate(new Date())
                        .changeDate(new Date())
                        .size(newDataURLDTO.getFile().get(0).getSize())
                        .build()
        );


        Mockito.when(dataModelRepository.saveAll(any())).thenReturn(dataModelList);

        assertInstanceOf(EmptyResponseDTO.class, dataService.saveFiles(newDataURLDTO)); // Проверка тела ответа
        assertEquals("Saved successfully", dataService.saveFiles(newDataURLDTO).getMessage());
        assertEquals(HttpStatus.OK, dataService.saveFiles(newDataURLDTO).getStatus());

        assertEquals(dataURLFileDTO.size(), dataService.mapDataURLDTOToModel(newDataURLDTO).size()); // Проверка маппинга
        assertEquals(dataURLFileDTO.get(0).getOriginalFilename(), dataService.mapDataURLDTOToModel(newDataURLDTO).get(0).getName());
        assertInstanceOf(byte[].class, dataService.mapDataURLDTOToModel(newDataURLDTO).get(0).getFile());
        assertEquals(dataURLFileDTO.get(0).getSize(), dataService.mapDataURLDTOToModel(newDataURLDTO).get(0).getFile().length);
        assertNotEquals(0, dataService.mapDataURLDTOToModel(newDataURLDTO).get(0).getFile().length);
    }


    @Test
    void saveFormDataFilesTest() throws IOException { // + вложенный protected mapFormDataDTOToModel
        String dataURL = "0J/RgNC40LLQtdGC"; // Входные данные
        byte[] decodedBytes = Base64.getDecoder().decode(dataURL);
        MultipartFile[] files = {new MockMultipartFile("file", decodedBytes)};
        NewFormDataDTO newFormDataDTO = NewFormDataDTO.builder()
                .comment("FormDataComment")
                .file(files)
                .build();


        Mockito.when(dataModelRepository.saveAll(any())).thenReturn(null);

        assertInstanceOf(EmptyResponseDTO.class, dataService.saveFiles(newFormDataDTO)); // Проверка ответа
        assertEquals("Saved successfully", dataService.saveFiles(newFormDataDTO).getMessage());
        assertEquals(HttpStatus.OK, dataService.saveFiles(newFormDataDTO).getStatus());

        assertEquals(newFormDataDTO.getFile().length, dataService.mapFormDataDTOToModel(newFormDataDTO).size()); // Проверка маппинга
        assertEquals(newFormDataDTO.getFile()[0].getOriginalFilename(), dataService.mapFormDataDTOToModel(newFormDataDTO).get(0).getName());
        assertInstanceOf(byte[].class, dataService.mapFormDataDTOToModel(newFormDataDTO).get(0).getFile());
        assertArrayEquals(newFormDataDTO.getFile()[0].getBytes(), dataService.mapFormDataDTOToModel(newFormDataDTO).get(0).getFile());
        assertEquals(newFormDataDTO.getFile()[0].getSize(), dataService.mapFormDataDTOToModel(newFormDataDTO).get(0).getFile().length);
        assertNotEquals(0, dataService.mapFormDataDTOToModel(newFormDataDTO).get(0).getFile().length);

    }

    @Test
    void getDataModelWithLinkDTOTest() {
        byte[] file = new byte[128];
        DataModel dataModel1 = DataModel.builder() // Для Mockito
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .file(file)
                .build();
        DataModel dataModel2 = DataModel.builder()
                .id(2L)
                .uploadDate(new Date(1668945269000L)) // 20.11.22
                .changeDate(new Date(1668945269000L))
                .name("file2.html")
                .type("text/html")
                .size(12L)
                .comment("second file")
                .file(file)
                .build();
        DataModel dataModel3 = DataModel.builder()
                .id(3L)
                .uploadDate(new Date(1669118069000L)) // 22.11.22
                .changeDate(new Date(1669118069000L))
                .name("script.js")
                .type("application/javascript")
                .size(12L)
                .comment("third file")
                .file(file)
                .build();

        String name1 = "file"; // Параметры на вход
        String name2 = "script";
        String date1 = "2022-11-19_00:00";
        String date2 = "2022-11-21_00:00";
        String type1 = "application/javascript";
        String type2 = "text/plain";
        String[] array1 = {type1};
        String[] array2 = {type2};
        String[] array12 = {type1, type2};

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType(eq(""), eq(new Date(0)), any(), eq("")))
                .thenReturn(List.of(dataModel1, dataModel2, dataModel3));

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType(eq(name1), eq(new Date(0)), any(), eq("")))
                .thenReturn(List.of(dataModel1, dataModel2));

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType(eq(name2), eq(new Date(0)), any(), eq("")))
                .thenReturn(List.of(dataModel3));

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType("", new Date(1668798000000L), new Date(1668970800000L), "")) // 19-21
                .thenReturn(List.of(dataModel1, dataModel2));

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType(eq(""), eq(new Date(0)), any(), eq("application/javascript")))
                .thenReturn(List.of(dataModel3));

        Mockito.when(dataModelRepository.getAllModelsFilterByNameAndDateAndType(eq(""), eq(new Date(0)), any(), eq("text/plain")))
                .thenReturn(List.of(dataModel1));

        // Без фильтра
        assertEquals(3, dataService.getDataModelWithLinkDTO(null, null, null, null).size());

        // Фильтр по имени
        assertEquals(2, dataService.getDataModelWithLinkDTO(name1, null, null, null).size());
        assertEquals("file1.txt", dataService.getDataModelWithLinkDTO(name1, null, null, null).get(0).getName());
        assertEquals(1, dataService.getDataModelWithLinkDTO(name2, null, null, null).size());
        assertEquals("script.js", dataService.getDataModelWithLinkDTO(name2, null, null, null).get(0).getName());

        // Фильтр по дате
        assertEquals(2, dataService.getDataModelWithLinkDTO(null, date1, date2, null).size());
        assertEquals("file1.txt", dataService.getDataModelWithLinkDTO(null, date1, date2, null).get(0).getName());
        assertEquals("file2.html", dataService.getDataModelWithLinkDTO(null, date1, date2, null).get(1).getName());

        // Фильтр по типу
        assertEquals(1, dataService.getDataModelWithLinkDTO(null, null, null, array1).size());
        assertEquals("script.js", dataService.getDataModelWithLinkDTO(null, null, null, array1).get(0).getName());
        assertEquals(1, dataService.getDataModelWithLinkDTO(null, null, null, array2).size());
        assertEquals("file1.txt", dataService.getDataModelWithLinkDTO(null, null, null, array2).get(0).getName());
        assertEquals(2, dataService.getDataModelWithLinkDTO(null, null, null, array12).size());

        // Невалидная дата

        assertThrows(ParsingDateException.class, () -> dataService.getDataModelWithLinkDTO(null, "32523", null, null), "Data Parsing Error");
    }

    @Test
    void getFileTest() {
        byte[] file = new byte[128];
        DataModel dataModel = DataModel.builder() // Данные на вход
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .file(file)
                .build();

        Mockito.when(dataModelRepository.existsById(-1L)).thenReturn(false);
        Mockito.when(dataModelRepository.existsById(1L)).thenReturn(true);

        Mockito.when(dataModelRepository.getReferenceById(1L)).thenReturn(dataModel);

        // Невалидный id
        assertThrows(FileIdNotFoundException.class, () -> dataService.getFile(-1L), "File Id Not Found Error");

        // Проверка тела ответа
        assertInstanceOf(byte[].class, dataService.getFile(1L).getBody());
        assertEquals(HttpStatus.OK, dataService.getFile(1L).getStatusCode());
        assertEquals(dataModel.getType(), Objects.requireNonNull(dataService.getFile(1L).getHeaders().getContentType()).toString());
        assertEquals("attachment; filename=" + "\"" + dataModel.getName() + "\"",
                dataService.getFile(1L).getHeaders().getContentDisposition().toString());
    }

    @Test
    void getZipTest() {
        Long[] id = {1L};
        Long[] emptyId = {};
        Long[] invalidId = {-1L};

        byte[] file = new byte[128]; // Данные на вход
        DataModel dataModel = DataModel.builder()
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .file(file)
                .build();

        Mockito.when(dataModelRepository.getReferenceById(id[0])).thenReturn(dataModel);
        Mockito.when(dataModelRepository.getReferenceById(invalidId[0])).thenThrow(new NullPointerException());

//        Mockito.when(dataModelRepository.getFileById(id[0])).thenReturn(dataModel.getFile());
//        Mockito.when(dataModelRepository.getNameById(id[0])).thenReturn(dataModel.getName());
//        Mockito.when(dataModelRepository.getNameById(invalidId[0])).thenThrow(new NullPointerException());

        // Проверка тела ответа
        assertEquals(HttpStatus.OK, dataService.getZip(id).getStatusCode());
        assertEquals("application/zip", Objects.requireNonNull(dataService.getZip(id).getHeaders().getContentType()).toString());
        assertEquals("attachment; filename=\"files.zip\"", dataService.getZip(id).getHeaders().getContentDisposition().toString());

        // Проверка ошибок
        assertThrows(FileIdNotFoundException.class, () -> dataService.getZip(emptyId), "File Id Not Found Error");
        assertThrows(FileIdNotFoundException.class, () -> dataService.getZip(invalidId), "File Id Not Found Error");
    }

    @Test
    void updateCommentByIDTest() {
        Long validId = 1L;
        Long invalidId = -1L;
        String comment = "some comment"; // Данные на вход

        Mockito.when(dataModelRepository.existsById(validId)).thenReturn(true);
        Mockito.when(dataModelRepository.existsById(invalidId)).thenReturn(false);

        // Проверка ошибки
        assertThrows(FileIdNotFoundException.class, () -> dataService.updateCommentById(invalidId, comment), "File Id Not Found Error");

        // Проверка тела ответа
        assertInstanceOf(EmptyResponseDTO.class, dataService.updateCommentById(validId, comment));
        assertEquals("Updated successfully", dataService.updateCommentById(validId, comment).getMessage());
        assertEquals(HttpStatus.OK, dataService.updateCommentById(validId, comment).getStatus());
    }

    @Test
    void deleteDataModelByIdTest() {
        Long validId = 1L;
        Long invalidId = -1L;

        Mockito.when(dataModelRepository.existsById(validId)).thenReturn(true);
        Mockito.when(dataModelRepository.existsById(invalidId)).thenReturn(false);

        // Проверка ошибки
        assertThrows(FileIdNotFoundException.class, () -> dataService.deleteDataModelById(invalidId), "File Id Not Found Error");

        // Проверка тела ответа
        assertInstanceOf(EmptyResponseDTO.class, dataService.deleteDataModelById(validId));
        assertEquals("Deleted successfully", dataService.deleteDataModelById(validId).getMessage());
        assertEquals(HttpStatus.OK, dataService.deleteDataModelById(validId).getStatus());
    }
}
