package com.kuchumov.springApp.controller;

import com.kuchumov.springApp.dto.DataModelWithLinkDTO;
import com.kuchumov.springApp.dto.EmptyResponseDTO;
import com.kuchumov.springApp.dto.DataURLDTO;
import com.kuchumov.springApp.dto.FormDataDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface FilesRestControllerDocumentation {

    @ApiOperation(value = "Загрузить новый файл (Formdata)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно сохранено"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    EmptyResponseDTO downloadFormData(@ModelAttribute FormDataDTO formdataDTO);

    @ApiOperation(value = "Загрузить новый файл (DataURL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно сохранено"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    EmptyResponseDTO downloadDataURL(@RequestBody DataURLDTO dataURLDTO);

    @ApiOperation(value = "Получить все имена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список имен")
    })
    List<String> getAllNames();

    @ApiOperation(value = "Получить все модели (с фильтром или без)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список моделей, удовлетворяющих фильтрам"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверные данные в фильтрах")
    })
    List<DataModelWithLinkDTO> getAllDataModels(@RequestParam(value = "name", required = false)
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
                                                String[] types);

    @ApiOperation(value = "Скачать файл по прямой ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ответ с файлом в теле"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    ResponseEntity<byte[]> sendFile(@PathVariable("id") @ApiParam(name = "id", value = "id файла", example = "1") Long id);

    @ApiOperation(value = "Скачать zip архив по прямой ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ответ с архивом в теле"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный(-е) id")
    })
    ResponseEntity<byte[]> sendZip(@RequestParam(value = "id") @ApiParam(name = "id", value = "id's файлов") Long[] id);

    @ApiOperation(value = "Редактировать комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отредактировано успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    EmptyResponseDTO updateComment(@RequestBody @ApiParam(value = "comment", example = "{\"comment\": \"update comment\"}") Map<String, String> comment, @PathVariable("id")
    @ApiParam(name = "id", value = "id файла", example = "1") Long id);

    @ApiOperation(value = "Удалить файл")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Удалено успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса. Неверный id")
    })
    EmptyResponseDTO deleteDataModelEntry(@PathVariable("id") @ApiParam(name = "id", value = "id файла", example = "1") Long id);
}
