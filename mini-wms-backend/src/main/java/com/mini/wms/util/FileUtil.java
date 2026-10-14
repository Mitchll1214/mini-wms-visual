package com.mini.wms.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * 文件工具类
 * 处理文件路径、目录创建、SHA256 计算等
 */
@Component
public class FileUtil {

    private String dataDir;
    private String imageDir;
    private String trashDir;
    private String backupDir;

    public FileUtil() {}

    @Value("${mini.wms.data-dir:./data}")
    public void setDataDir(String dataDir) { this.dataDir = dataDir; }

    @Value("${mini.wms.image-dir:./data/images}")
    public void setImageDir(String imageDir) { this.imageDir = imageDir; }

    @Value("${mini.wms.trash-dir:./data/trash}")
    public void setTrashDir(String trashDir) { this.trashDir = trashDir; }

    @Value("${mini.wms.backup-dir:./data/backup}")
    public void setBackupDir(String backupDir) { this.backupDir = backupDir; }

    public String getDataDir() { return dataDir; }
    public String getImageDir() { return imageDir; }
    public String getTrashDir() { return trashDir; }
    public String getBackupDir() { return backupDir; }

    /**
     * 获取文件存储基础目录
     */
    public Path getBasePath() {
        return Paths.get(dataDir);
    }

    /**
     * 创建目录（如果不存在）
     */
    public void ensureDirectoryExists(String relativePath) {
        Path path = getBasePath().resolve(relativePath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("创建目录失败: " + path, e);
        }
    }

    /**
     * 获取商品图片存储目录
     */
    public String getGoodsImageDir(int goodsId) {
        return Paths.get(imageDir, "goods", String.valueOf(goodsId)).toString();
    }

    /**
     * 按用途类型构建图片相对路径（分区存储）
     * type: goods / bin / supplier / main（商品主图）
     * 例：goods/{goodsId}/{uuid}.jpg、bin/{binId}/{uuid}.jpg、supplier/{supplierId}/{uuid}.jpg
     */
    public String buildImageRelativePath(String type, Integer bizId, String fileName) {
        String safeType = (type == null || type.isEmpty()) ? "goods" : type;
        return Paths.get(safeType, String.valueOf(bizId), fileName).toString();
    }

    /**
     * 按 imageType 生成缩略图相对路径
     */
    public String buildThumbRelativePath(String relativePath) {
        return getThumbPath(relativePath);
    }

    /**
     * 生成缩略图路径
     */
    public String getThumbPath(String relativePath) {
        String dir = Paths.get(relativePath).getParent().toString();
        String fileName = Paths.get(relativePath).getFileName().toString();
        String ext = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }
        return Paths.get(dir, fileName + "_thumb" + ext).toString();
    }

    /**
     * 计算文件 SHA256
     */
    public String sha256(Path path) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(path);
            md.update(data, 0, data.length);
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 算法不可用", e);
        }
    }

    /**
     * 生成唯一文件名
     */
    public String generateUniqueFileName(String ext) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (ext != null && !ext.isEmpty() ? ext : ".jpg");
    }

    /**
     * 移动文件到回收站
     */
    public Path moveToTrash(Path source) throws IOException {
        ensureDirectoryExists(trashDir);
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String destDir = Paths.get(trashDir, date).toString();
        ensureDirectoryExists(destDir);
        Path dest = Paths.get(destDir, source.getFileName().toString());
        Files.move(source, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }
}
