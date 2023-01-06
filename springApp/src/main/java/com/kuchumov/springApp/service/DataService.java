package com.kuchumov.springApp.service;

import com.kuchumov.springApp.dto.*;
import com.kuchumov.springApp.entity.DataModel;
import com.kuchumov.springApp.exceptionHandler.customExceptions.*;
import com.kuchumov.springApp.repository.DataModelRepository;
import com.kuchumov.springApp.utilites.mappers.DataModelMapper;
import com.kuchumov.springApp.utilites.transliterator.Transliterator;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Data
public class DataService {
    private DataModelRepository dataModelRepository;

    @PersistenceContext
    private EntityManager em;

    public DataService(DataModelRepository dataModelRepository) {
        this.dataModelRepository = dataModelRepository;
    }

    public EmptyResponseDTO saveFiles(DataURLDTO dataURLDTO) { // Сохранить несколько файлов dataURL
        if (dataURLDTO.getFile().size() == 1) {
            return saveFile(dataURLDTO.getFile().get(0), dataURLDTO.getComment());
        }
        List<DataModel> dataModelList = mapDataURLDtoToModel(dataURLDTO);
        dataModelRepository.saveAll(dataModelList);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected EmptyResponseDTO saveFile(DataURLFileDTO dataURLFile, String comment) { // Сохранить один файл dataURL
        DataModel dataModel;

        String filePath = saveFileOnDisk(dataURLFile.getDataURL(), dataURLFile.getOriginalFilename());

        dataModel = DataModelMapper.MAPPER.dataURLFileToModel(dataURLFile, comment, filePath);
        dataModelRepository.save(dataModel);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected List<DataModel> mapDataURLDtoToModel(DataURLDTO dataURLDTO) {
        List<DataURLFileDTO> dataURLFileList = dataURLDTO.getFile();
        List<DataModel> dataModelList = new ArrayList<>();

        for (DataURLFileDTO dataURLFile : dataURLFileList) {

            String filePath = saveFileOnDisk(dataURLFile.getDataURL(), dataURLFile.getOriginalFilename());

            dataModelList.add(DataModelMapper.MAPPER.dataURLFileToModel(dataURLFile, dataURLDTO.getComment(), filePath));
        }

        return dataModelList;
    }

    public EmptyResponseDTO saveFiles(FormDataDTO formdataDTO) { // Сохранить несколько файлов formdata
        if (formdataDTO.getFile().length == 1) {
            return saveFile(formdataDTO.getFile()[0], formdataDTO.getComment());
        }
        List<DataModel> dataModelList = mapFormDataDtoToModel(formdataDTO);
        dataModelRepository.saveAll(dataModelList);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected EmptyResponseDTO saveFile(MultipartFile multipartFile, String comment) { // Сохранить один файл formdata
        DataModel dataModel;
        try {
            String filePath = saveFileOnDisk(multipartFile.getBytes(), multipartFile.getOriginalFilename());
            dataModel = DataModelMapper.MAPPER.formDataFileToModel(multipartFile, comment, filePath);
        } catch (IOException e) {
            throw new ParsingFormDataException("ParsingFormDataException. " +
                    "Failed to extract file. Try again later", e);
        }
        dataModelRepository.save(dataModel);
        return new EmptyResponseDTO(HttpStatus.OK, "Saved successfully");
    }

    protected List<DataModel> mapFormDataDtoToModel(FormDataDTO formdataDTO) {
        MultipartFile[] multipartFileList = formdataDTO.getFile();
        List<DataModel> dataModelList = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFileList) {
            try {
                String filePath = saveFileOnDisk(multipartFile.getBytes(), multipartFile.getOriginalFilename());
                dataModelList.add(DataModelMapper.MAPPER.formDataFileToModel(multipartFile, formdataDTO.getComment(), filePath));
            } catch (IOException e) {
                throw new ParsingFormDataException("ParsingFormDataException. " +
                        "Failed to extract file. Try again later", e);
            }
        }
        return dataModelList;
    }

    protected String saveFileOnDisk(String dataURL, String name) {
        byte[] decodedBytes = dataURLToByte(dataURL);
        return saveFileOnDisk(decodedBytes, name);
    }

    protected String saveFileOnDisk(byte[] file, String name) {
        checkBaseDirectory(); // Проверка директории, куда сохранять
        String uniqueFilePath = getUniqueFilePath(name); // Уникальный путь (для дубликатов)
        try (FileOutputStream fos = new FileOutputStream(uniqueFilePath)) {
            fos.write(file);
        } catch (IOException e) {
            throw new FileLoadingException("Ошибка при сохранении файла", e);
        }
        return uniqueFilePath;
    }

    private byte[] dataURLToByte(String dataURL) {
        return Base64.getDecoder().decode(dataURL);
    }

    private void checkBaseDirectory() {
        File directory = new File("saved_files");
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private String getUniqueFilePath(String name) {
        String originalFilePath = "saved_files/" + name;
        File path = new File(originalFilePath);
        if (path.exists()) {
            Date date = new Date();
            originalFilePath = "saved_files/" + date.getTime();
            File directory = new File(originalFilePath);
            if (directory.exists()) {
                while (directory.exists()) {
                    date = new Date();
                    originalFilePath = "saved_files/" + date.getTime();
                    directory = new File(originalFilePath);
                }
            }
            directory.mkdir();
            return originalFilePath + "/" + name;
        } else {
            return originalFilePath;
        }
    }

    public List<String> getAllNames() {
        return dataModelRepository.getAllNames();
    }

    @Transactional
    public List<DataModelWithLinkDTO> getDataModelsWithLink(String name, String date1,
                                                            String date2, String[] types) {

        List<DataModel> results = getFilteredDataModelsList(name, date1, date2, types); // Получаем фильтрованные модели

        List<DataModelWithLinkDTO> dataModelsWithLink = new ArrayList<>();
        for (DataModel dm : results) { // Добавляем к моделям ссылку на скачивание
            dataModelsWithLink.add(DataModelMapper.MAPPER.modelToDataModelWithLinkDTO(dm, getDownloadLink(dm.getId())));
        }
        return dataModelsWithLink; // Возвращаем
    }

    protected List<DataModel> getFilteredDataModelsList(String name, String date1,
                                                      String date2, String[] types) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DataModel> cr = cb.createQuery(DataModel.class);
        Root<DataModel> root = cr.from(DataModel.class);

        Predicate[] predicates = getPredicates(cb, root, name, date1, date2, types); // Получаем динамически изменяемые предикаты

        cr.select(root).where(predicates); // Выполняем по ним запрос
        return em.createQuery(cr).getResultList();
    }

    private Predicate[] getPredicates(CriteriaBuilder cb, Root<DataModel> root, String name, String date1,
                              String date2, String[] types) {
        int predicateCount = 2; // Как минимум два фильтра есть всегда (поиск по пустой строке и по максимальному интервалу)

        List<Date> dates;

        if (name == null) { // Если фильтра по имени нет, то принимаем за пустую строку, (LIKE запросе проигнорирует)
            name = "";
        }

        if (date1 == null && date2 == null) { // Если фильтра по времени нет, то интервал "01.01.1970 - данный момент"
            Date dateFrom = new Date(0);
            Date dateTo = new Date();
            dates = List.of(dateFrom, dateTo);
        } else if (date1 != null && date2 == null) { // Если фильтр по первой дате, то интервал "первая дата - данный момент"
            Date dateFrom = parseDate(date1);
            Date dateTo = new Date();
            dates = List.of(dateFrom, dateTo);
        } else if (date1 == null) {
            Date dateFrom = new Date(0); // Если фильтр по второй дате, то интервал "01.01.1970 - вторая дата"
            Date dateTo = parseDate(date2);
            dates = List.of(dateFrom, dateTo);
        } else {
            dates = parseDate(date1, date2); // Если фильтр по двум датам, то интервал "первая дата - вторая дата"
        }

        if (types != null && types.length > 0) { // Если есть фильтр по типам, то изменяем количество предикатов
            predicateCount += types.length;
        }

        Predicate[] predicates = new Predicate[predicateCount]; // Два стандартных предиката
        predicates[0] = cb.between(root.get("changeDate"), dates.get(0), dates.get(1));
        predicates[1] = cb.like(root.get("name"), "%" + name + "%");

        for (int i = 2; i < predicateCount; i++) { // Предикаты по типу, если такие имеются
            predicates[i] = cb.like(root.get("type"), "%" + types[i - 2] + "%");
        }

        return predicates;
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
        return "/files/" + id;
    }

    @Transactional
    public ResponseEntity<byte[]> getFile(Long id) {
        if (!dataModelRepository.existsById(id)) {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }

        DataModel dataModel = dataModelRepository.getReferenceById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", dataModel.getType());

        String filename = encodeToUTF8(dataModel.getName());
        byte[] file = getFileFromDisk(dataModel.getFilePath());

        headers.set("Content-Disposition", "attachment;filename=" + filename);
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(file);
    }

    private String encodeToUTF8(String filename) {
        String URLEncodedFileName;
        URLEncodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return URLEncodedFileName.replace('+', ' ');
    }

    protected byte[] getFileFromDisk(String filePath) {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (IOException e) {
            throw new FileLoadingException("Ошибка получения файла", e);
        }
        return bytes;
    }

    @Transactional
    public ResponseEntity<byte[]> getZip(Long[] id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/zip");
        headers.set("Content-Disposition", "attachment;filename=files.zip");
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(getFilesZipArray(id));
    }

    private byte[] getFilesZipArray(Long[] files) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        try {
            if (files.length != 0) {
                for (Long file : files) {
                    DataModel dataModel = dataModelRepository.getReferenceById(file);

                    byte[] input = getFileFromDisk(dataModel.getFilePath());
                    String zipEntryName = Transliterator.transliterate(dataModel.getName());
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

    @Transactional
    public EmptyResponseDTO updateCommentById(Long id, String comment) {
        if (dataModelRepository.findById(id).isPresent()) {
            DataModel dataModel = dataModelRepository.findById(id).get();
            dataModel.setComment(comment);
            dataModel.setChangeDate(new Date());
            dataModelRepository.save(dataModel);
            return new EmptyResponseDTO(HttpStatus.OK, "Updated successfully");
        } else {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }
    }

    @Transactional
    public EmptyResponseDTO deleteDataModelById(Long id) {
        if (dataModelRepository.existsById(id)) {
            dataModelRepository.deleteById(id);
            return new EmptyResponseDTO(HttpStatus.OK, "Deleted successfully");
        } else {
            throw new FileIdNotFoundException("Bad file id. File with this id not found");
        }
    }
}
