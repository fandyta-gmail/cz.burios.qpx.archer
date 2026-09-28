package cz.burios.qpx.darwin.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FILESTORE_RECORD")
public class FileStoreRecord {
    @Id @Column(name = "ID", length = 20, nullable = false) private String id;
    @Column(name = "FILESTORE_ID", length = 20, nullable = false) private String fileStoreId;
    @Column(name = "NAME", length = 255, nullable = false) private String name;
    @Column(name = "ORIGINAL_NAME", length = 255, nullable = false) private String originalName;
    @Column(name = "EXTENSION", length = 32) private String extension;
    @Column(name = "CONTENT_TYPE", length = 255) private String contentType;
    @Column(name = "SIZE", nullable = false) private Long size;
    @Column(name = "STORAGE_KEY", length = 255, nullable = false) private String storageKey;
    @Column(name = "CHECKSUM", length = 64) private String checksum;
    @Column(name = "CREATED_AT", nullable = false) private Instant createdAt;
    @Column(name = "UPDATED_AT", nullable = false) private Instant updatedAt;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getFileStoreId() { return fileStoreId; } public void setFileStoreId(String v) { fileStoreId = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getOriginalName() { return originalName; } public void setOriginalName(String v) { originalName = v; }
    public String getExtension() { return extension; } public void setExtension(String v) { extension = v; }
    public String getContentType() { return contentType; } public void setContentType(String v) { contentType = v; }
    public Long getSize() { return size; } public void setSize(Long v) { size = v; }
    public String getStorageKey() { return storageKey; } public void setStorageKey(String v) { storageKey = v; }
    public String getChecksum() { return checksum; } public void setChecksum(String v) { checksum = v; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant v) { createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant v) { updatedAt = v; }
}
