package com.kuchumov.springApp.service;

import com.kuchumov.springApp.DTO.*;
import com.kuchumov.springApp.entity.DataModel;
import com.kuchumov.springApp.exceptionHandler.customExceptions.FileIdNotFoundException;
import com.kuchumov.springApp.exceptionHandler.customExceptions.ParsingDateException;
import com.kuchumov.springApp.exceptionHandler.customExceptions.ParsingFormDataException;
import com.kuchumov.springApp.exceptionHandler.customExceptions.ZipCreatingException;
import com.kuchumov.springApp.repository.DataModelRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Data
@NoArgsConstructor
public class DataService {
    private DataModelRepository dataModelRepository;

    @Autowired
    public DataService(DataModelRepository dataModelRepository) {
        this.dataModelRepository = dataModelRepository;
    }

    public EmptyResponseDTO saveFiles(NewDataURLDTO newDataURLDTO) {
        List<DataModel> dataModelList = mapDataURLDTOToModel(newDataURLDTO);
        dataModelRepository.saveAll(dataModelList);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected List<DataModel> mapDataURLDTOToModel(NewDataURLDTO newDataURLDTO) {
        List<DataURLFileDTO> dataURLFileList = newDataURLDTO.getFile();
        List<DataModel> dataModelList = new ArrayList<>();

        for (DataURLFileDTO dataURLFile : dataURLFileList) {

            String dataURL = dataURLFile.getDataURL();
            byte[] decodedBytes = Base64.getDecoder().decode(dataURL);

            dataModelList.add(DataModel.builder()
                    .changeDate(new Date())
                    .comment(newDataURLDTO.getComment())
                    .file(decodedBytes)
                    .type(dataURLFile.getContentType())
                    .name(dataURLFile.getOriginalFilename())
                    .size(dataURLFile.getSize())
                    .uploadDate(new Date())
                    .build());
        }

        return dataModelList;
    }

    public EmptyResponseDTO saveFiles(NewFormDataDTO newFormDataDTO) {
        List<DataModel> dataModelList = mapFormDataDTOToModel(newFormDataDTO);
            dataModelRepository.saveAll(dataModelList);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected List<DataModel> mapFormDataDTOToModel(NewFormDataDTO newFormDataDTO) {
        MultipartFile[] formDataFileList = newFormDataDTO.getFile();
        List<DataModel> dataModelList = new ArrayList<>();
        for (MultipartFile formDataFile : formDataFileList) {
            try {
                dataModelList.add(DataModel.builder()
                        .changeDate(new Date())
                        .comment(newFormDataDTO.getComment())
                        .file(formDataFile.getBytes())
                        .type(formDataFile.getContentType())
                        .name(formDataFile.getOriginalFilename())
                        .size(formDataFile.getSize())
                        .uploadDate(new Date())
                        .build());
            } catch (IOException e) {
                throw new ParsingFormDataException("ParsingFormDataException. " +
                        "Failed to extract file. Try again later", e);
            }
        }
        return dataModelList;
    }

    public List<String> getAllNames() {
        return dataModelRepository.getAllNames();
    }

    @Transactional
    public List<DataModelWithLinkDTO> getDataModelWithLinkDTO(String name, String date1,
                                                              String date2, String[] types) {
        List<DataModel> dataModels = new ArrayList<>();
        List<Date> dates;

        if (name == null) {
            name = "";
        }

        if (date1 == null && date2 == null) {
            Date dateFrom = new Date(0);
            Date dateTo = new Date();
            dates = List.of(dateFrom, dateTo);
        } else if (date1 != null && date2 == null) {
            Date dateFrom = parseDate(date1);
            Date dateTo = new Date();
            dates = List.of(dateFrom, dateTo);
        } else if (date1 == null) {
            Date dateFrom = new Date(0);
            Date dateTo = parseDate(date2);
            dates = List.of(dateFrom, dateTo);
        } else {
            dates = parseDate(date1, date2);
        }

        if (types == null) {
            types = new String[]{""};
        }

        for (String type: types) {
                dataModels.addAll(dataModelRepository.getAllModelsFilterByNameAndDateAndType(name, dates.get(0), dates.get(1), type));
            }

        List<DataModelWithLinkDTO> dataModelsWithLink = new ArrayList<>();
        for (DataModel dm : dataModels ) {
            dataModelsWithLink.add(DataModelWithLinkDTO.builder()
                    .id(dm.getId())
                    .changeDate(dm.getChangeDate())
                    .comment(dm.getComment())
                    .downloadLink(getDownloadLink(dm.getId()))
                    .type(dm.getType())
                    .name(dm.getName())
                    .size(dm.getSize())
                    .uploadDate(dm.getUploadDate())
                    .build());
        }
        return dataModelsWithLink;
    }

    private List<Date> parseDate(String date1, String date2) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
            Date dateFrom = formatter.parse(date1);
            Date dateTo = formatter.parse(date2);
            return List.of(dateFrom, dateTo);
        } catch (ParseException e) {
            throw new ParsingDateException("ParsingDateException. Failed to parse date. " +
                    "Date format must be 'yyyy-MM-dd_HH:mm'", e);
        }
    }

    private Date parseDate(String date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new ParsingDateException("ParsingDateException. Failed to parse date. " +
                    "Date format must be 'yyyy-MM-dd_HH:mm'", e);
        }

    }

    private String getDownloadLink(Long id) {
        return "/files/"+ id;
    }

    @Transactional
    public ResponseEntity<?> getFile(Long id) {
        if (!dataModelRepository.existsById(id)) {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }

        DataModel dataModel = dataModelRepository.getReferenceById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", dataModel.getType());

        String filename = encodeToUTF8(dataModel.getName());

        headers.set("Content-Disposition","attachment;filename=" + filename);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(dataModel.getFile());
    }

    private String encodeToUTF8(String filename) {
        String URLEncodedFileName;
        URLEncodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return URLEncodedFileName.replace('+', ' ');
    }
    @Transactional
    public ResponseEntity<?> getZip(Long[] id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/zip");
        headers.set("Content-Disposition","attachment;filename=files.zip");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(getFilesZipArray(id));
    }

    private byte[] getFilesZipArray(Long[] files) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        try {
            if (files.length != 0) {
                for (Long file : files) {
                    DataModel dataModel = dataModelRepository.getReferenceById(file);

                    byte[] input = dataModel.getFile();
                    String zipEntryName = transliterate(dataModel.getName());
                    ZipEntry entry = new ZipEntry(zipEntryName);
                    entry.setSize(input.length);
                    zos.putNextEntry(entry);
                    zos.write(input);
                    zos.closeEntry();
                }
                zos.close();
            } else {
                throw new FileIdNotFoundException("ids is null");
            }
        } catch (NullPointerException | EntityNotFoundException e) {
            throw new FileIdNotFoundException("Неверный id", e);
        } catch (IOException e) {
            throw new ZipCreatingException("ZipCreatingException. Try again later", e);
        }

        return bos.toByteArray();
    }

    private String transliterate(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        String transliteratedStr;

        Map<String, String> conformity = new HashMap<>();
        conformity.put("А", "A");
        conformity.put("Б", "B");
        conformity.put("В", "V");
        conformity.put("Г", "G");
        conformity.put("Д", "D");
        conformity.put("Е", "YE");
        conformity.put("Ё", "YO");
        conformity.put("Ж", "G");
        conformity.put("З", "Z");
        conformity.put("И", "I");
        conformity.put("Й", "Y");
        conformity.put("К", "K");
        conformity.put("Л", "L");
        conformity.put("М", "M");
        conformity.put("Н", "N");
        conformity.put("О", "O");
        conformity.put("П", "P");
        conformity.put("Р", "R");
        conformity.put("С", "S");
        conformity.put("Т", "T");
        conformity.put("У", "U");
        conformity.put("Ф", "F");
        conformity.put("Х", "H");
        conformity.put("Ц", "C");
        conformity.put("Ч", "CH");
        conformity.put("Ш", "SH");
        conformity.put("Щ", "SH");
        conformity.put("Ъ", "");
        conformity.put("Ы", "Y");
        conformity.put("Ь", "");
        conformity.put("Э", "YE");
        conformity.put("Ю", "YU");
        conformity.put("Я", "YA");

        for (int i = 0; i < str.length(); i++) {
            String key = str.substring(i, i + 1);
            if (conformity.containsKey(key.toUpperCase())) {
                if (conformity.containsKey(key)) {
                    stringBuilder.append(conformity.get(key));
                } else {
                    stringBuilder.append(conformity.get(key.toUpperCase()).toLowerCase());
                }
            } else if ((int) key.charAt(0) > 127) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append(key);
            }
        }
        transliteratedStr = stringBuilder.toString();
        return transliteratedStr;
    }

    @Transactional
    public EmptyResponseDTO updateCommentById(Long id, String comment) {
        if (dataModelRepository.existsById(id)) {
            dataModelRepository.updateCommentById(id, comment, new Date());
            return new EmptyResponseDTO(HttpStatus.OK, "Updated successfully");
        } else {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }
    }

    @Transactional
    public EmptyResponseDTO deleteDataModelById(Long id) {
        if (dataModelRepository.existsById(id)) {
            dataModelRepository.deleteDataModelById(id);
            return new EmptyResponseDTO(HttpStatus.OK, "Deleted successfully");
        } else {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }
    }
}
