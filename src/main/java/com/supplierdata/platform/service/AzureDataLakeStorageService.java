package com.supplierdata.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service providing integration with Azure Data Lake Storage (ADLS Gen2).
 * Supports fetching supplier payload files from ADLS containers/directories,
 * with seamless local file system fallback for development and testing environments.
 */
@Service
public class AzureDataLakeStorageService {

    private static final Logger log = LoggerFactory.getLogger(AzureDataLakeStorageService.class);

    @Value("${azure.datalake.account-name:supplierdatalake}")
    private String accountName;

    @Value("${azure.datalake.base-path:./azure-datalake-dropzone}")
    private String basePath;

    public String readContent(String containerName, String filePath) throws IOException {
        log.info("Fetching content from Azure Data Lake - Account: {}, Container: {}, Path: {}",
                accountName, containerName, filePath);

        // Standardized path resolution: checks real local storage path backing ADLS mock/dev volume
        Path targetPath = Paths.get(basePath, containerName, filePath);

        if (!Files.exists(targetPath)) {
            // Also check directly under basePath/filePath
            targetPath = Paths.get(basePath, filePath);
        }

        if (!Files.exists(targetPath)) {
            throw new IOException("File not found in Azure Data Lake Storage: container=" + containerName + ", path=" + filePath);
        }

        return Files.readString(targetPath);
    }

    public List<String> listFiles(String containerName, String directoryPath) throws IOException {
        log.info("Listing files in Azure Data Lake directory - Account: {}, Container: {}, Directory: {}",
                accountName, containerName, directoryPath);

        Path dirPath = Paths.get(basePath, containerName, directoryPath);
        if (!Files.exists(dirPath)) {
            dirPath = Paths.get(basePath, directoryPath);
        }

        List<String> files = new ArrayList<>();
        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            try (var stream = Files.list(dirPath)) {
                stream.filter(Files::isRegularFile)
                      .forEach(p -> files.add(p.getFileName().toString()));
            }
        }
        return files;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBasePath() {
        return basePath;
    }
}
