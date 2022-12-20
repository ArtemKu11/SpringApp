package com.kuchumov.springApp.controller;

import com.kuchumov.springApp.DTO.DataModelWithLinkDTO;
import com.kuchumov.springApp.DTO.EmptyResponseDTO;
import com.kuchumov.springApp.DTO.NewDataURLDTO;
import com.kuchumov.springApp.DTO.NewFormDataDTO;
import com.kuchumov.springApp.service.DataService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FilesRestController {
    private final DataService dataService;

    @Autowired
    public FilesRestController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping(path = "/new/formdata", consumes = "multipart/form-data") // Загрузить новый файл (FormData)
    @ApiOperation(value = "Загрузить новый файл (Formdata)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно сохранено"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    EmptyResponseDTO downloadFormData(@ModelAttribute NewFormDataDTO newFormDataDTO) {
        return dataService.saveFiles(newFormDataDTO);
    }
    @PostMapping(path = "/new/dataURL", consumes = "application/json") // Загрузить новый файл (DataURL)
    @ApiOperation(value = "Загрузить новый файл (DataURL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно сохранено"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    EmptyResponseDTO downloadDataURL(@RequestBody NewDataURLDTO newDataURLDTO) {
        return dataService.saveFiles(newDataURLDTO);
    }

    @GetMapping("/names") // Получить все имена
    @ApiOperation(value = "Получить все имена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список имен")
    })
    List<String> getAllNames() {
        return dataService.getAllNames();
    }

    @GetMapping() // Получить все модели (с фильтром или без)
    @ApiOperation(value = "Получить все модели (с фильтром или без)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список моделей, удовлетворяющих фильтрам"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверные данные в фильтрах")
    })
    List<DataModelWithLinkDTO> getAllModels(@RequestParam(value = "name", required = false)
                                            @ApiParam(name = "name", value = "Искомое имя. Возможно частичное вхождение",
                                            example = "file") String name,
                                            @RequestParam(value = "from", required = false)
                                            @ApiParam(name = "from", value = "Начальная дата в формате \"yyyy-MM-dd_HH:mm\"",
                                                    example = "2022-12-12_12:00") String date1,
                                            @RequestParam(value = "to", required = false)
                                            @ApiParam(name = "to", value = "Конечная дата в формате \"yyyy-MM-dd_HH:mm\"",
                                                    example = "2022-12-12_13:00") String date2,
                                            @RequestParam(value = "type", required = false)
                                            @ApiParam(name = "type", value = "Искомые типы. Возможно частичное вхождение")
                                            String[] types) {
        return dataService.getDataModelWithLinkDTO(name, date1, date2, types);
    }

    @GetMapping("/{id}")  // Прямая ссылка на файл
    @ApiOperation(value = "Скачать файл по прямой ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ответ с файлом в теле"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    ResponseEntity<?> sendFile(@PathVariable("id") @ApiParam(name = "id", value = "id файла", example = "1") Long id) {
        return dataService.getFile(id);
    }

    @GetMapping("/download") // Прямая ссылка на zip с файлами
    @ApiOperation(value = "Скачать zip архив по прямой ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ответ с архивом в теле"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный(-е) id")
    })
    ResponseEntity<?> sendZip(@RequestParam(value = "id") @ApiParam(name = "id", value = "id's файлов") Long[] id) {
        return dataService.getZip(id);
    }

    @PatchMapping("/{id}") // Редактировать комментарий
    @ApiOperation(value = "Редактировать комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отредактировано успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    EmptyResponseDTO updateFile(@RequestBody @ApiParam(value = "comment", example = "{\"comment\": \"update comment\"}") Map<String, String> comment, @PathVariable("id")
                                @ApiParam(name = "id", value = "id файла", example = "1") Long id) {
        return dataService.updateCommentById(id, comment.get("comment"));
    }

    @DeleteMapping("/{id}") // Удалить файл
    @ApiOperation(value = "Удалить файл")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Удалено успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    EmptyResponseDTO deleteFile(@PathVariable("id") @ApiParam(name = "id", value = "id файла", example = "1") Long id) {
        return dataService.deleteDataModelById(id);
    }

}
