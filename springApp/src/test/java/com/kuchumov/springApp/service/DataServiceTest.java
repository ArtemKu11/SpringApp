package com.kuchumov.springApp.service;

import com.kuchumov.springApp.dto.DataURLFileDTO;
import com.kuchumov.springApp.dto.EmptyResponseDTO;
import com.kuchumov.springApp.dto.DataURLDTO;
import com.kuchumov.springApp.dto.FormDataDTO;
import com.kuchumov.springApp.entity.DataModel;
import com.kuchumov.springApp.exceptionHandler.customExceptions.FileIdNotFoundException;
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

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataServiceTest {

    @InjectMocks
    DataService dataService;

    @Mock
    DataModelRepository dataModelRepository;


    @Test
    void saveFilesDataURLTest() {
        // Данные на вход
        DataURLFileDTO dataURLFile1 = DataURLFileDTO.builder()  // Первый файл
                .dataURL("0J/RgNC40LLQtdGC")
                .contentType("text/html")
                .originalFilename("privet.html")
                .size(12L)
                .build();

        DataURLFileDTO dataURLFile2 = DataURLFileDTO.builder() // Второй файл
                .dataURL("0J/RgNC40LLQtdGC")
                .contentType("text/html")
                .originalFilename("privet2.html")
                .size(12L)
                .build();

        List<DataURLFileDTO> dataURLFileList = List.of(dataURLFile1, dataURLFile2); // Лист из них

        // Входные ДТОшки
        DataURLDTO dataURLDTO1 = new DataURLDTO("dataURLComment", dataURLFileList.subList(0, 1)); // Один файл
        DataURLDTO dataURLDTO2 = new DataURLDTO("dataURLComment", dataURLFileList); // Несколько файлов


        DataService spy = Mockito.spy(dataService); // Для подмены сохранения на диск
        doReturn("somePath").when(spy).saveFileOnDisk((String) any(), any()); // Сохранять на диск не надо

        assertInstanceOf(EmptyResponseDTO.class, spy.saveFiles(dataURLDTO1)); // Если один файл
        assertEquals(HttpStatus.OK, spy.saveFiles(dataURLDTO1).getStatus());
        assertEquals("Saved successfully", spy.saveFiles(dataURLDTO1).getMessage());
        verify(spy, times(3)).saveFile((DataURLFileDTO) any(), any());

        assertInstanceOf(EmptyResponseDTO.class, spy.saveFiles(dataURLDTO2)); // Если несколько файлов
        assertEquals(HttpStatus.OK, spy.saveFiles(dataURLDTO2).getStatus());
        assertEquals("Saved successfully", spy.saveFiles(dataURLDTO2).getMessage());
        verify(spy, times(3)).saveFile((DataURLFileDTO) any(), any());

        assertEquals(dataURLDTO2.getFile().size(), spy.mapDataURLDtoToModel(dataURLDTO2).size()); // Проверка маппинга
        assertEquals(dataURLDTO2.getFile().get(0).getOriginalFilename(), spy.mapDataURLDtoToModel(dataURLDTO2).get(0).getName());
        assertEquals("somePath", spy.mapDataURLDtoToModel(dataURLDTO2).get(0).getFilePath());
        assertEquals(dataURLDTO2.getFile().get(0).getSize(), spy.mapDataURLDtoToModel(dataURLDTO2).get(0).getSize());
    }

    @Test
    void saveFilesFormDataTest() {
        // Данные на вход

        MultipartFile[] files1 = {new MockMultipartFile("file1", new byte[123])};
        MultipartFile[] files2 = {new MockMultipartFile("file1", new byte[123]),
                new MockMultipartFile("file2", new byte[123])};

        // Входные ДТОшки
        FormDataDTO formDataDTO1 = FormDataDTO.builder() // С одним файлом
                .file(files1)
                .comment("One file DTO")
                .build();

        FormDataDTO formDataDTO2 = FormDataDTO.builder() // С двумя файлами
                .file(files2)
                .comment("Two files DTO")
                .build();

        DataService spy = Mockito.spy(dataService); // Для подмены сохранения на диск
        doReturn("somePath").when(spy).saveFileOnDisk((byte[]) any(), any()); // Сохранять на диск не надо


        assertInstanceOf(EmptyResponseDTO.class, spy.saveFiles(formDataDTO1)); // Если один файл
        assertEquals(HttpStatus.OK, spy.saveFiles(formDataDTO1).getStatus());
        assertEquals("Saved successfully", spy.saveFiles(formDataDTO1).getMessage());
        verify(spy, times(3)).saveFile((MultipartFile) any(), any());

        assertInstanceOf(EmptyResponseDTO.class, spy.saveFiles(formDataDTO2)); // Если несколько файлов
        assertEquals(HttpStatus.OK, spy.saveFiles(formDataDTO2).getStatus());
        assertEquals("Saved successfully", spy.saveFiles(formDataDTO2).getMessage());
        verify(spy, times(3)).saveFile((MultipartFile) any(), any());

        assertEquals(formDataDTO2.getFile().length, spy.mapFormDataDtoToModel(formDataDTO2).size()); // Проверка маппинга
        assertEquals(formDataDTO2.getFile()[0].getOriginalFilename(), spy.mapFormDataDtoToModel(formDataDTO2).get(0).getName());
        assertEquals("somePath", spy.mapFormDataDtoToModel(formDataDTO2).get(0).getFilePath());
        assertEquals(formDataDTO2.getFile()[0].getSize(), spy.mapFormDataDtoToModel(formDataDTO2).get(0).getSize());
    }

    @Test
    void getDataModelsWithLinkTest() {
        DataModel dataModel1 = DataModel.builder() // Для Mockito
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .filePath("somePath1")
                .build();
        DataModel dataModel2 = DataModel.builder()
                .id(2L)
                .uploadDate(new Date(1668945269000L)) // 20.11.22
                .changeDate(new Date(1668945269000L))
                .name("file2.html")
                .type("text/html")
                .size(12L)
                .comment("second file")
                .filePath("somePath2")
                .build();
        DataModel dataModel3 = DataModel.builder()
                .id(3L)
                .uploadDate(new Date(1669118069000L)) // 22.11.22
                .changeDate(new Date(1669118069000L))
                .name("script.js")
                .type("application/javascript")
                .size(12L)
                .comment("third file")
                .filePath("somePath3")
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

        DataService spy = Mockito.spy(dataService); // Для подмены CriteriaApi

        doReturn(List.of(dataModel1, dataModel2, dataModel3)).when(spy)
                .getFilteredDataModelsList(null, null, null, null);

        doReturn(List.of(dataModel1, dataModel2)).when(spy)
                .getFilteredDataModelsList(name1, null, null, null);

        doReturn(List.of(dataModel3)).when(spy)
                .getFilteredDataModelsList(name2, null, null, null);

        doReturn(List.of(dataModel1, dataModel2)).when(spy)
                .getFilteredDataModelsList(null, date1, date2, null);

        doReturn(List.of(dataModel3)).when(spy)
                .getFilteredDataModelsList(null, null, null, array1);

        doReturn(List.of(dataModel1)).when(spy)
                .getFilteredDataModelsList(null, null, null, array2);

        doReturn(List.of(dataModel1, dataModel3)).when(spy)
                .getFilteredDataModelsList(null, null, null, array12);

        // Без фильтра
        assertEquals(3, spy.getDataModelsWithLink(null, null, null, null).size());

        // Фильтр по имени
        assertEquals(2, spy.getDataModelsWithLink(name1, null, null, null).size());
        assertEquals("file1.txt", spy.getDataModelsWithLink(name1, null, null, null).get(0).getName());
        assertEquals(1, spy.getDataModelsWithLink(name2, null, null, null).size());
        assertEquals("script.js", spy.getDataModelsWithLink(name2, null, null, null).get(0).getName());

        // Фильтр по дате
        assertEquals(2, spy.getDataModelsWithLink(null, date1, date2, null).size());
        assertEquals("file1.txt", spy.getDataModelsWithLink(null, date1, date2, null).get(0).getName());
        assertEquals("file2.html", spy.getDataModelsWithLink(null, date1, date2, null).get(1).getName());

        // Фильтр по типу
        assertEquals(1, spy.getDataModelsWithLink(null, null, null, array1).size());
        assertEquals("script.js", spy.getDataModelsWithLink(null, null, null, array1).get(0).getName());
        assertEquals(1, spy.getDataModelsWithLink(null, null, null, array2).size());
        assertEquals("file1.txt", spy.getDataModelsWithLink(null, null, null, array2).get(0).getName());
        assertEquals(2, spy.getDataModelsWithLink(null, null, null, array12).size());

    }

    @Test
    void getFileTest() {
        byte[] file = new byte[128];
        DataModel dataModel = DataModel.builder() // Данные для Mockito
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .filePath("somePath")
                .build();

        DataService spy = Mockito.spy(dataService); // Для подмены загрузки файла с диска
        doReturn(file).when(spy).getFileFromDisk(any());

        Mockito.when(dataModelRepository.existsById(-1L)).thenReturn(false);
        Mockito.when(dataModelRepository.existsById(1L)).thenReturn(true);

        Mockito.when(dataModelRepository.getReferenceById(1L)).thenReturn(dataModel);

        // Невалидный id
        assertThrows(FileIdNotFoundException.class, () -> spy.getFile(-1L), "File Id Not Found Error");

        // Проверка тела ответа
        assertInstanceOf(byte[].class, spy.getFile(1L).getBody());
        assertEquals(HttpStatus.OK, spy.getFile(1L).getStatusCode());
        assertEquals(dataModel.getType(), Objects.requireNonNull(spy.getFile(1L).getHeaders().getContentType()).toString());
        assertEquals("attachment; filename=" + "\"" + dataModel.getName() + "\"",
                spy.getFile(1L).getHeaders().getContentDisposition().toString());
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
                .filePath("somePath")
                .build();

        Mockito.when(dataModelRepository.getReferenceById(id[0])).thenReturn(dataModel);
        Mockito.when(dataModelRepository.getReferenceById(invalidId[0])).thenThrow(new NullPointerException());

        DataService spy = Mockito.spy(dataService); // Для подмены загрузки файла с диска
        doReturn(file).when(spy).getFileFromDisk(any());


        // Проверка тела ответа
        assertEquals(HttpStatus.OK, spy.getZip(id).getStatusCode());
        assertEquals("application/zip", Objects.requireNonNull(spy.getZip(id).getHeaders().getContentType()).toString());
        assertEquals("attachment; filename=\"files.zip\"", spy.getZip(id).getHeaders().getContentDisposition().toString());

        // Проверка ошибок
        assertThrows(FileIdNotFoundException.class, () -> spy.getZip(emptyId), "File Id Not Found Error");
        assertThrows(FileIdNotFoundException.class, () -> spy.getZip(invalidId), "File Id Not Found Error");
    }

    @Test
    void updateCommentByIDTest() {
        DataModel dataModel = DataModel.builder()
                .id(1L)
                .uploadDate(new Date(1668858869000L)) // 19.11.22
                .changeDate(new Date(1668858869000L))
                .name("file1.txt")
                .type("text/plain")
                .size(12L)
                .comment("first file")
                .filePath("somePath")
                .build();

        Long validId = 1L;
        Long invalidId = -1L;
        String comment = "some comment"; // Данные на вход
        Optional<DataModel> empty = Optional.empty();
        Optional<DataModel> notEmpty = Optional.of(dataModel);

        Mockito.when(dataModelRepository.findById(validId)).thenReturn(notEmpty);
        Mockito.when(dataModelRepository.findById(invalidId)).thenReturn(empty);

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
