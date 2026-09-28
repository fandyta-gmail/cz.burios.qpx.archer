package cz.burios.qpx.darwin.filestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.servlet.ServletContext;

import org.springframework.stereotype.Component;

/** Physical file storage. Logical names never become filesystem paths. */
@Component
public class FileStorage {
    private static final String ROOT_PARAMETER = "qpx.filestore.default.root";

    private final Path root;

    public FileStorage(ServletContext servletContext) {
        String configured = servletContext.getInitParameter(ROOT_PARAMETER);
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Missing ServletContext parameter: " + ROOT_PARAMETER);
        }
        try {
            root = Path.of(configured).toAbsolutePath().normalize();
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize FileStore root", e);
        }
    }

    public Path root() {
        return root;
    }

    public String newStorageKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Path path(String storageKey) {
        if (storageKey == null || !storageKey.matches("[0-9a-fA-F]{32}")) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        Path result = root.resolve(storageKey.substring(0, 2)).resolve(storageKey).normalize();
        if (!result.startsWith(root)) {
            throw new IllegalArgumentException("Storage path escapes root");
        }
        return result;
    }

    public Path store(InputStream input, String storageKey) throws IOException {
        Path target = path(storageKey);
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(root, ".upload-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                input.transferTo(out);
            }
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return target;
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

    public InputStream open(String storageKey) throws IOException {
        return Files.newInputStream(path(storageKey));
    }

    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(path(storageKey));
    }
}
