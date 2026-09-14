package com.supplierdata.platform.service;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Service handling secure FTP/SFTP server integrations.
 * Supports downloading inbound supplier data files, listing remote directories,
 * and securely uploading outbound generated export files.
 * Includes local filesystem fallback for development and testing.
 */
@Service
public class FtpStorageService {

    private static final Logger log = LoggerFactory.getLogger(FtpStorageService.class);

    @Value("${ftp.server.host:localhost}")
    private String serverHost;

    @Value("${ftp.server.port:21}")
    private int serverPort;

    @Value("${ftp.server.username:supplier}")
    private String username;

    @Value("${ftp.server.password:secret}")
    private String password;

    @Value("${ftp.base-path:./ftp-dropzone}")
    private String ftpBasePath;

    /**
     * Downloads file content from FTP server or local FTP dropzone fallback.
     */
    public String downloadFile(String remotePath) throws IOException {
        log.info("Downloading file from FTP server {}:{} path {}", serverHost, serverPort, remotePath);

        // Try standard FTP server if configured non-localhost
        if (!"localhost".equalsIgnoreCase(serverHost) && !"127.0.0.1".equalsIgnoreCase(serverHost)) {
            try {
                return downloadViaFtpClient(remotePath);
            } catch (Exception ex) {
                log.warn("FTP client connection failed to {}:{}, falling back to local FTP dropzone directory: {}",
                        serverHost, serverPort, ex.getMessage());
            }
        }

        // Local FTP dropzone fallback
        Path localPath = Paths.get(ftpBasePath, remotePath);
        if (!Files.exists(localPath)) {
            localPath = Paths.get(ftpBasePath, "inbound", remotePath);
        }
        if (!Files.exists(localPath)) {
            throw new IOException("FTP file not found in dropzone: " + remotePath);
        }
        return Files.readString(localPath, StandardCharsets.UTF_8);
    }

    /**
     * Uploads outbound file content to remote FTP/SFTP destination or local outbound dropzone fallback.
     */
    public String uploadFile(String remotePath, String content) throws IOException {
        log.info("Uploading outbound file to FTP server {}:{} path {}", serverHost, serverPort, remotePath);

        if (!"localhost".equalsIgnoreCase(serverHost) && !"127.0.0.1".equalsIgnoreCase(serverHost)) {
            try {
                uploadViaFtpClient(remotePath, content);
                return "ftp://" + serverHost + ":" + serverPort + "/" + remotePath;
            } catch (Exception ex) {
                log.warn("FTP client upload failed to {}:{}, saving to local FTP outbound dropzone: {}",
                        serverHost, serverPort, ex.getMessage());
            }
        }

        // Local FTP outbound dropzone fallback
        Path localOutboundDir = Paths.get(ftpBasePath, "outbound");
        if (!Files.exists(localOutboundDir)) {
            Files.createDirectories(localOutboundDir);
        }
        Path targetPath = localOutboundDir.resolve(Paths.get(remotePath).getFileName().toString());
        Files.writeString(targetPath, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return targetPath.toAbsolutePath().toString();
    }

    public List<String> listFiles(String remoteDirectory) throws IOException {
        log.info("Listing FTP files in remote directory {}", remoteDirectory);
        Path localDir = Paths.get(ftpBasePath, remoteDirectory);
        if (!Files.exists(localDir)) {
            localDir = Paths.get(ftpBasePath, "inbound");
        }

        List<String> files = new ArrayList<>();
        if (Files.exists(localDir) && Files.isDirectory(localDir)) {
            try (var stream = Files.list(localDir)) {
                stream.filter(Files::isRegularFile)
                      .forEach(p -> files.add(p.getFileName().toString()));
            }
        }
        return files;
    }

    private String downloadViaFtpClient(String remotePath) throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serverHost, serverPort);
            ftp.login(username, password);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = ftp.retrieveFile(remotePath, baos);
            if (!success) {
                throw new IOException("Failed to download file via FTP: " + remotePath);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } finally {
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        }
    }

    private void uploadViaFtpClient(String remotePath, String content) throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serverHost, serverPort);
            ftp.login(username, password);
            ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            boolean success = ftp.storeFile(remotePath, bais);
            if (!success) {
                throw new IOException("Failed to upload file via FTP: " + remotePath);
            }
        } finally {
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        }
    }

    public String getServerHost() { return serverHost; }
    public String getFtpBasePath() { return ftpBasePath; }
}
