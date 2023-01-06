package com.kuchumov.springApp.controller;

import com.kuchumov.springApp.dto.DataModelWithLinkDTO;
import com.kuchumov.springApp.dto.EmptyResponseDTO;
import com.kuchumov.springApp.dto.DataURLDTO;
import com.kuchumov.springApp.dto.FormDataDTO;
import com.kuchumov.springApp.service.DataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/files")
public class FilesRestController implements FilesRestControllerDocumentation {
    private final DataService dataService;

    @PostMapping(path = "/new/formdata", consumes = "multipart/form-data") // Загрузить новый файл (FormData)
    public EmptyResponseDTO downloadFormData(@ModelAttribute FormDataDTO formdataDTO) {
        return dataService.saveFiles(formdataDTO);
    }
    @PostMapping(path = "/new/dataURL", consumes = "application/json") // Загрузить новый файл (DataURL)
    public EmptyResponseDTO downloadDataURL(@RequestBody DataURLDTO dataURLDTO) {
        return dataService.saveFiles(dataURLDTO);
    }

    @GetMapping("/names") // Получить все имена
    public List<String> getAllNames() {
        return dataService.getAllNames();
    }

    @GetMapping() // Получить все модели (с фильтром или без)
    public List<DataModelWithLinkDTO> getAllDataModels(@RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "from", required = false) String date1,
                                                @RequestParam(value = "to", required = false) String date2,
                                                @RequestParam(value = "type", required = false) String[] types) {
        return dataService.getDataModelsWithLink(name, date1, date2, types);
    }

    @GetMapping("/{id}")  // Прямая ссылка на файл
    public ResponseEntity<byte[]> sendFile(@PathVariable("id") Long id) {
        return dataService.getFile(id);
    }

    @GetMapping("/download") // Прямая ссылка на zip с файлами
    public ResponseEntity<byte[]> sendZip(@RequestParam(value = "id") Long[] id) {
        return dataService.getZip(id);
    }

    @PatchMapping("/{id}") // Редактировать комментарий
    public EmptyResponseDTO updateComment(@RequestBody Map<String, String> comment, @PathVariable("id") Long id) {
        return dataService.updateCommentById(id, comment.get("comment"));
    }

    @DeleteMapping("/{id}") // Удалить файл
    public EmptyResponseDTO deleteDataModelEntry(@PathVariable("id") Long id) {
        return dataService.deleteDataModelById(id);
    }

}
