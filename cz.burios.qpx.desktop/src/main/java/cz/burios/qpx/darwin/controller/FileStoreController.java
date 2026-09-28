package cz.burios.qpx.darwin.controller;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cz.burios.qpx.darwin.filestore.FileStoreService;
import cz.burios.uniql.model.DynamicRecord;

@RestController
@RequestMapping("/api/filestore")
public class FileStoreController {

    private final FileStoreService service;

    public FileStoreController(FileStoreService service) {
        this.service = service;
    }

    @GetMapping("/directories")
    public List<DynamicRecord> listDirectories(
            @RequestParam(name = "parentId", required = false) String parentId) throws Exception {
        return service.listDirectories(parentId);
    }

    @PostMapping("/directories")
    public ResponseEntity<DynamicRecord> createDirectory(
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam("name") String name) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createDirectory(parentId, name));
    }

    @GetMapping("/directories/{id}")
    public ResponseEntity<DynamicRecord> findDirectory(@PathVariable String id) throws Exception {
        DynamicRecord record = service.findDirectory(id);
        return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
    }

    @GetMapping("/files")
    public List<DynamicRecord> listFiles(@RequestParam("fileStoreId") String fileStoreId)
            throws Exception {
        return service.listFiles(fileStoreId);
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DynamicRecord> upload(
            @RequestParam("fileStoreId") String fileStoreId,
            @RequestPart("file") MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try (InputStream input = file.getInputStream()) {
            DynamicRecord record = service.storeFile(
                    fileStoreId,
                    originalName,
                    file.getContentType(),
                    input,
                    file.getSize());

            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        }
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<DynamicRecord> findFile(@PathVariable String id) throws Exception {
        DynamicRecord record = service.findFile(id);
        return record == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(record);
    }

    @GetMapping("/files/{id}/content")
    public ResponseEntity<Resource> download(@PathVariable String id) throws Exception {
        DynamicRecord record = service.findFile(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        InputStream input = service.openFile(id);
        String contentType = record.getString("CONTENT_TYPE");
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String originalName = record.getString("ORIGINAL_NAME");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(originalName).build());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(((Number) record.get("SIZE")).longValue())
                .headers(headers)
                .body(new InputStreamResource(input));
    }
}
