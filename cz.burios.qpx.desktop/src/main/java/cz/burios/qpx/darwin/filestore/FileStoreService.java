package cz.burios.qpx.darwin.filestore;

import java.security.DigestInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;

import cz.burios.uniql.model.DynamicRecord;

@Service
public class FileStoreService {
    private final FileStoreRepository repository;
    private final FileStorage storage;

    public FileStoreService(FileStoreRepository repository, FileStorage storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public DynamicRecord createDirectory(String parentId, String name) throws Exception {
        parentId = normalizeId(parentId);
        validateName(name);
        if (parentId != null && repository.findDirectory(parentId) == null) {
            throw new IllegalArgumentException("Parent directory not found: " + parentId);
        }
        String id = newId();
        Instant now = Instant.now();

        DynamicRecord record = new DynamicRecord();
        record.put("ID", id);
        record.put("PARENT_ID", parentId);
        record.put("NAME", name);
        record.put("CREATED_AT", now);
        record.put("UPDATED_AT", now);
        repository.insertDirectory(record);
        return repository.findDirectory(id);
    }

    public DynamicRecord findDirectory(String id) throws Exception {
        return repository.findDirectory(id);
    }

    public List<DynamicRecord> listDirectories(String parentId) throws Exception {
        return repository.listDirectories(parentId);
    }

    public DynamicRecord storeFile(String fileStoreId, String originalName,
            String contentType, InputStream input, long size) throws Exception {
        if (fileStoreId == null || fileStoreId.isBlank()) {
            throw new IllegalArgumentException("FileStore directory is required");
        }
        if (repository.findDirectory(fileStoreId) == null) {
            throw new IllegalArgumentException("FileStore directory not found: " + fileStoreId);
        }
        validateName(originalName);
        if (size < 0) throw new IllegalArgumentException("Invalid file size");

        String id = newId();
        String storageKey = storage.newStorageKey();
        String extension = extension(originalName);
        Instant now = Instant.now();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        storage.store(new DigestInputStream(input, digest), storageKey);
        boolean committed = false;
        try {
            String checksum = HexFormat.of().formatHex(digest.digest());
            long actualSize = Files.size(storage.path(storageKey));
            if (actualSize != size) {
                throw new IllegalArgumentException("File size mismatch");
            }

            DynamicRecord record = new DynamicRecord();
            record.put("ID", id);
            record.put("FILESTORE_ID", fileStoreId);
            record.put("NAME", originalName);
            record.put("ORIGINAL_NAME", originalName);
            record.put("EXTENSION", extension);
            record.put("CONTENT_TYPE", contentType);
            record.put("SIZE", actualSize);
            record.put("STORAGE_KEY", storageKey);
            record.put("CHECKSUM", checksum);
            record.put("CREATED_AT", now);
            record.put("UPDATED_AT", now);
            repository.insertFile(record);
            committed = true;
            return repository.findFile(id);
        } finally {
            if (!committed) storage.delete(storageKey);
        }
    }

    public DynamicRecord findFile(String id) throws Exception {
        return repository.findFile(id);
    }

    public InputStream openFile(String id) throws Exception {
        DynamicRecord record = findFile(id);
        if (record == null) throw new IllegalArgumentException("File not found: " + id);
        return storage.open(record.getString("STORAGE_KEY"));
    }

    public List<DynamicRecord> listFiles(String fileStoreId) throws Exception {
        return repository.listFiles(fileStoreId);
    }

    private static String normalizeId(String id) {
        return id == null || id.isBlank() ? null : id;
    }

    private String newId() {
        return storage.newStorageKey().substring(0, 20);
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 && dot < name.length() - 1 ? name.substring(dot + 1).toLowerCase() : null;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || ".".equals(name) || "..".equals(name)
                || name.contains("/") || name.contains("\\") || name.length() > 255) {
            throw new IllegalArgumentException("Invalid file or directory name");
        }
    }
}
