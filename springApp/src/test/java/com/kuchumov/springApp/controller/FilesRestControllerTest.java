package com.kuchumov.springApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kuchumov.springApp.DTO.DataURLFileDTO;
import com.kuchumov.springApp.DTO.NewDataURLDTO;
import com.kuchumov.springApp.entity.DataModel;
import com.kuchumov.springApp.repository.DataModelRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilesRestControllerTest {
    private final MockMvc mockMvc;
    private final DataModelRepository dataModelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectWriter objectWriter = objectMapper.writer();
    private Long lastId = 4L;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public FilesRestControllerTest(MockMvc mockMvc, DataModelRepository dataModelRepository) {
        this.mockMvc = mockMvc;
        this.dataModelRepository = dataModelRepository;
    }

    @BeforeAll
    void setup() { // Инициализация тестовой БД + добавление в нее 3 записей
        List<DataModel> dataModels = getDataModels();
        dataModelRepository.saveAll(dataModels); // create-drop автоматически удалит. БД тестовая в тестовом контейнере
    }

    private List<DataModel> getDataModels() { // Создание 3 записей
        String[] files = {"src/test/resources/testFiles/index.html", "src/test/resources/testFiles/script.js", "src/test/resources/testFiles/style.css"};
        Date[] dates = {new Date(1668927600000L), new Date(1669014000000L), new Date(1669186800000L)}; // 20, 21, 23.11.22 12:00
        String[] names = {"index.html", "script.js", "style.css"};
        String[] types = {"text/html", "application/javascript", "text/css"};
        String[] comments = {"first file comment", "second file comment", "third file comment"};

        List<DataModel> dataModels = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            byte[] file = getFile(files[i]);
            DataModel dataModel = DataModel.builder()
                    .uploadDate(dates[i])
                    .changeDate(dates[i])
                    .name(names[i])
                    .type(types[i])
                    .size((long) file.length)
                    .comment(comments[i])
                    .file(file)
                    .build();
            dataModels.add(dataModel);
        }

        return dataModels;
    }

    private byte[] getFile(String path) { // 3 тестовых файла
        File file = new File(path);
        return new byte[(int) file.length()];
    }

    private NewDataURLDTO getNewDataURLDTO() { // Для теста сохранения dataURL


        DataURLFileDTO dataURLFileDTO = DataURLFileDTO.builder()
                .dataURL("0J/RgNC40LLQtdGC")
                .contentType("text/html")
                .originalFilename("privet.html")
                .size(12L)
                .build();

        return NewDataURLDTO.builder()
                .comment("dataURLComment")
                .file(List.of(dataURLFileDTO))
                .build();
    }

    @Test
    @Transactional
    void downloadDataURLTest() throws Exception { // Тест сохранения dataURL

        NewDataURLDTO newDataURLDTO = getNewDataURLDTO();
        String body = objectWriter.writeValueAsString(newDataURLDTO); // Входные JSON данные

        mockMvc.perform(MockMvcRequestBuilders.post("/files/new/dataURL") // Проверка тела ответа
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Saved successfully"));

        DataModel referenceById = dataModelRepository.getReferenceById(this.lastId);

        assertEquals(4, dataModelRepository.getAllNames().size()); // Проверка изменения БД
        assertEquals("privet.html", referenceById.getName());
        assertEquals("dataURLComment", referenceById.getComment());
        assertEquals(12L, Long.valueOf(referenceById.getFile().length));
        assertEquals(referenceById.getSize(), Long.valueOf(referenceById.getFile().length));

        long diffInMilliesUD = new Date().getTime() - referenceById.getUploadDate().getTime();
        long diffInMilliesCD = new Date().getTime() - referenceById.getChangeDate().getTime();

        assertTrue(500L > diffInMilliesUD); // Проверка установки текущего времени
        assertTrue(500L > diffInMilliesCD);
        this.lastId += 1;
    }

    @Test
    @Transactional
    void downloadFormDataURL() throws Exception { // Тест сохранения FormData

        String dataURL = "0J/RgNC40LLQtdGC"; // html размером 12 байт
        byte[] decodedBytes = Base64.getDecoder().decode(dataURL);

        MockMultipartFile file // Входные данные
                = new MockMultipartFile(
                "file",
                "privet.html",
                MediaType.TEXT_HTML_VALUE,
                decodedBytes
        );

        mockMvc.perform(multipart("/files/new/formdata") // Проверка тела ответа
                        .file(file)
                        .param("comment", "FormData comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Saved successfully"));

        DataModel referenceById = dataModelRepository.getReferenceById(this.lastId);

        assertEquals(4, dataModelRepository.getAllNames().size()); // Проверка изменения БД
        assertEquals("privet.html", referenceById.getName());
        assertEquals("FormData comment", referenceById.getComment());
        assertEquals(12L, Long.valueOf(referenceById.getFile().length));
        assertEquals(referenceById.getSize(), Long.valueOf(referenceById.getFile().length));

        long diffInMilliesUD = new Date().getTime() - referenceById.getUploadDate().getTime();
        long diffInMilliesCD = new Date().getTime() - referenceById.getChangeDate().getTime();

        assertTrue(500L > diffInMilliesUD); // Проверка установки текущего времени
        assertTrue(500L > diffInMilliesCD);
        this.lastId += 1;
    }

    @Test
    void getAllNamesTest() throws Exception { // Тест получения списка имен
        mockMvc.perform(MockMvcRequestBuilders.get("/files/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)) // Проверка наличия заранее добавленных файлов
                .andExpect(jsonPath("$.[0]").value("index.html"))
                .andExpect(jsonPath("$.[1]").value("script.js"))
                .andExpect(jsonPath("$.[2]").value("style.css"));
    }

    @Test
    void getAllModelsTest() throws Exception { // Тест получения списка моделей с фильтрами
        // 20.11.22, 21.11.22, 23.11.22, 12:00
        // index.html, script.js, style.css
        assertEquals(3, dataModelRepository.getAllNames().size());

        mockMvc.perform(MockMvcRequestBuilders.get("/files")) // Без фильтров
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].uploadDate").value("2022-11-20T07:00:00.000+00:00"))
                .andExpect(jsonPath("$.[0].changeDate").value("2022-11-20T07:00:00.000+00:00"))
                .andExpect(jsonPath("$.[0].type").value("text/html"))
                .andExpect(jsonPath("$.[0].size").value(904))
                .andExpect(jsonPath("$.[0].comment").value("first file comment"))
                .andExpect(jsonPath("$.[0].downloadLink").value("/files/1"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?name=s")) // Фильтр по имени
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(2))
                .andExpect(jsonPath("$.[1].id").value(3));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?from=2022-11-19_13:26&to=2022-11-21_13:27")) // Фильтр по дате (от-до)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?from=2022-11-19_13:26")) // Фильтр по дате (от->)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[2].id").value(3));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?to=2022-11-19_13:26")) // Фильтр по дате (<-до (ничего не найдено))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?to=2022-11-25_13:26")) // Фильтр по дате (<-до (найдено все))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[2].id").value(3));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?to=2022-11-22_13:26")) // Фильтр по дате (<-до (найдено частично))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?to=2022-11-22_136")) // Фильтр по дате (невалидный формат)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("ParsingDateException. Failed to parse date. Date format must be 'yyyy-MM-dd_HH:mm'"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?type=text/html")) // Фильтр по типу (полное соответствие)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?type=html")) // Фильтр по типу (частичное)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(1));

        mockMvc.perform(MockMvcRequestBuilders.get("/files?name=s&from=2022-11-21_00:26&to=2022-11-24_13:27&type=css")) // Все сразу
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].length()").value(8))
                .andExpect(jsonPath("$.[0].id").value(3));
    }

    @Test
    void sendFileTest() throws Exception { // Проверка прямой ссылки на файл
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/files/1"))
                .andExpect(status().isOk())
                .andReturn();

        assertInstanceOf(byte[].class, result.getResponse().getContentAsByteArray()); // Проверка тела ответа
        assertEquals(904, result.getResponse().getContentAsByteArray().length);
        assertEquals(200, result.getResponse().getStatus());
        assertEquals("text/html", result.getResponse().getContentType());
        assertEquals("attachment;filename=index.html", result.getResponse().getHeader("Content-Disposition"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/-1")) // Невалидный ид
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad file id. File with this id not found"));
    }

    @Test
    void sendZipTest() throws Exception { // Проверка ссылки на zip
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/files/download?id=1,2"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus()); // Проверка тела ответа
        assertEquals("application/zip", result.getResponse().getContentType());
        assertEquals("attachment;filename=files.zip", result.getResponse().getHeader("Content-Disposition"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/download?id=1,2,3,4")) // Невалидный ид
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Неверный id"));
    }

    @Test
    @Transactional
    void updateFileTest() throws Exception { // Проверка изменения комментария

        Map<String, String> body = new HashMap<>(); // Входные данные
        body.put("comment", "updatedComment");
        String jsonBody = objectWriter.writeValueAsString(body);

        mockMvc.perform(MockMvcRequestBuilders.patch("/files/1") // Проверка тела ответа
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Updated successfully"));

        DataModel referenceByIdAfterChange = dataModelRepository.getReferenceById(1L); // Проверка изменения состояния БД
        assertEquals("updatedComment", referenceByIdAfterChange.getComment());

        long diffInMilliesCD = new Date().getTime() - referenceByIdAfterChange.getChangeDate().getTime();

        assertEquals("index.html", referenceByIdAfterChange.getName());
        assertEquals("updatedComment", referenceByIdAfterChange.getComment());
        assertTrue(500L > diffInMilliesCD); // Проверка изменения времени
        this.lastId += 1;

        mockMvc.perform(MockMvcRequestBuilders.get("/files/-1")) // Проверка ошибки
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad file id. File with this id not found"));
    }

    @Test
    @Transactional
    void deleteFileTest() throws Exception { // Проверка удаления файла
        Long id = 1L;
        assertTrue(dataModelRepository.existsById(id));

        mockMvc.perform(MockMvcRequestBuilders.delete("/files/" + id) // Проверка тела ответа
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Deleted successfully"));

        assertFalse(dataModelRepository.existsById(id)); // Проверка изменения состояния БД

        mockMvc.perform(MockMvcRequestBuilders.get("/files/-1")) // Проверка ошибки
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad file id. File with this id not found"));
    }
}