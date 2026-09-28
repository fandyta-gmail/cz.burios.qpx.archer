package cz.burios.qpx.darwin.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "FILESTORE", indexes = @Index(name = "UX_FILESTORE_PARENT_NAME", columnList = "PARENT_ID,NAME", unique = true))
public class FileStore {
    @Id @Column(name = "ID", length = 20, nullable = false) private String id;
    @Column(name = "PARENT_ID", length = 20) private String parentId;
    @Column(name = "NAME", length = 255, nullable = false) private String name;
    @Column(name = "CREATED_AT", nullable = false) private Instant createdAt;
    @Column(name = "UPDATED_AT", nullable = false) private Instant updatedAt;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getParentId() { return parentId; } public void setParentId(String parentId) { this.parentId = parentId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
