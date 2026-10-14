package com.mini.wms.service;

import com.mini.wms.dto.Result;
import com.mini.wms.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 备份服务
 * 提供数据库和文件的备份功能
 */
@Service
@RequiredArgsConstructor
public class BackupService {

    private final FileUtil fileUtil;

    /**
     * 创建一键备份
     * @return ZIP文件的相对路径
     */
    @Transactional
    public Result<?> createBackup() {
        try {
            // 生成备份目录和文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDirName = "backup_" + timestamp;
            Path backupDir = Paths.get(fileUtil.getBackupDir(), backupDirName);
            Files.createDirectories(backupDir);

            // 备份数据库
            backupDatabase(backupDir);

            // 备份图片等文件
            backupFiles(backupDir);

            // 创建ZIP文件
            String zipFileName = backupDirName + ".zip";
            Path zipFilePath = Paths.get(fileUtil.getBackupDir(), zipFileName);
            createZipFile(backupDir, zipFilePath);

            // 清理临时目录
            deleteDirectory(backupDir);

            // 返回相对路径
            String relativePath = "backup/" + zipFileName;
            return Result.success(relativePath);
        } catch (Exception e) {
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 备份数据库（复制SQLite文件）
     */
    private void backupDatabase(Path backupDir) throws IOException {
        String dbFileName = "mini_wms.db";
        Path sourceDb = Paths.get(fileUtil.getDataDir(), dbFileName);
        Path targetDb = Paths.get(backupDir.toString(), dbFileName);

        if (Files.exists(sourceDb)) {
            Files.copy(sourceDb, targetDb, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 备份文件目录（图片等）
     */
    private void backupFiles(Path backupDir) throws IOException {
        // 备份图片目录
        Path sourceImageDir = Paths.get(fileUtil.getImageDir());
        Path targetImageDir = Paths.get(backupDir.toString(), "images");
        if (Files.exists(sourceImageDir)) {
            copyDirectory(sourceImageDir, targetImageDir);
        }

        // 备份其他目录（如果需要）
        // 例如：备份配置文件等
    }

    /**
     * 创建ZIP文件
     */
    private void createZipFile(Path sourceDir, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("压缩文件失败: " + path, e);
                        }
                    });
        }
    }

    /**
     * 复制目录
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    try {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("复制文件失败: " + sourcePath, e);
                    }
                });
    }

    /**
     * 删除目录及其内容
     */
    private void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted((p1, p2) -> p2.compareTo(p1)) // 删除子文件/目录再删除父目录
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException("删除文件失败: " + path, e);
                    }
                });
    }
}