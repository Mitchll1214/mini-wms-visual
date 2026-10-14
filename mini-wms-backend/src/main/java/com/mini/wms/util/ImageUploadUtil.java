package com.mini.wms.util;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.GoodsImage;
import com.mini.wms.entity.SupplierImage;
import com.mini.wms.mapper.ImageMapper;
import com.mini.wms.mapper.SupplierImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 图片上传工具类
 * 图片按用途类型分区存储：goods/{goodsId}、main/{goodsId}、bin/{binId}、supplier/{supplierId}
 */
@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    private final FileUtil fileUtil;
    private final ImageMapper imageMapper;
    private final SupplierImageMapper supplierImageMapper;
    private final ThumbnailsUtil thumbnailsUtil;

    /**
     * 保存商品相关图片（goods 相册 / main 主图 / bin 储位图）
     */
    public GoodsImage saveImage(MultipartFile file, Integer goodsId, String imageType, Integer binId) throws IOException {
        // 验证文件
        String ext = validate(file);
        // 按用途类型分区存储
        String safeType = (imageType == null || imageType.isEmpty()) ? "goods" : imageType;
        int bizId = goodsId != null ? goodsId : (binId != null ? binId : 0);
        SavedFile saved = writeFile(file, ext, safeType, bizId);

        // 保存到数据库
        GoodsImage image = new GoodsImage();
        image.setGoodsId(goodsId);
        image.setImageType(safeType);
        image.setBinId(binId);
        image.setRelativePath(saved.relativePath);
        image.setThumbPath(saved.thumbRelativePath);
        image.setMimeType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setSha256(saved.sha256);
        image.setCreatedAt(LocalDateTime.now());
        imageMapper.insert(image);

        return image;
    }

    /**
     * 保存供应商资质图片
     */
    public SupplierImage saveSupplierImage(MultipartFile file, Integer supplierId, String imageType) throws IOException {
        String ext = validate(file);
        String safeType = (imageType == null || imageType.isEmpty()) ? "supplier" : imageType;
        SavedFile saved = writeFile(file, ext, safeType, supplierId);

        SupplierImage image = new SupplierImage();
        image.setSupplierId(supplierId);
        image.setImageType(safeType);
        image.setRelativePath(saved.relativePath);
        image.setThumbPath(saved.thumbRelativePath);
        image.setMimeType(file.getContentType());
        image.setFileSize(file.getSize());
        image.setSha256(saved.sha256);
        image.setCreatedAt(LocalDateTime.now());
        supplierImageMapper.insert(image);

        return image;
    }

    /**
     * 校验文件扩展名与大小，返回小写扩展名
     */
    private String validate(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new RuntimeException("文件名为空");
        }
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = originalFileName.substring(dotIndex).toLowerCase();
        }
        if (!".jpg".equals(ext) && !".jpeg".equals(ext) && !".png".equals(ext) && !".webp".equals(ext)) {
            throw new RuntimeException("不支持的文件类型: " + ext);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("文件大小超过限制: 10MB");
        }
        return ext;
    }

    /**
     * 落盘原图 + 生成缩略图，返回相对路径
     */
    private SavedFile writeFile(MultipartFile file, String ext, String type, Integer bizId) throws IOException {
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;
        String relativePath = fileUtil.buildImageRelativePath(type, bizId, uniqueName);
        Path fullPath = Paths.get(fileUtil.getImageDir(), relativePath).toAbsolutePath();

        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath.toFile());

        String thumbRelativePath = fileUtil.buildThumbRelativePath(relativePath);
        Path thumbFullPath = Paths.get(fileUtil.getImageDir(), thumbRelativePath).toAbsolutePath();
        thumbnailsUtil.generateThumbnail(fullPath, thumbFullPath, 300, 300);

        String sha256 = fileUtil.sha256(fullPath);
        return new SavedFile(relativePath, thumbRelativePath, sha256);
    }

    private record SavedFile(String relativePath, String thumbRelativePath, String sha256) {}

    /**
     * 获取商品图片路径
     */
    public Path getImagePath(Integer imageId) {
        GoodsImage image = imageMapper.selectById(imageId);
        if (image == null) {
            return null;
        }
        return Paths.get(fileUtil.getImageDir(), image.getRelativePath());
    }

    /**
     * 获取商品缩略图路径
     */
    public Path getThumbPath(Integer imageId) {
        GoodsImage image = imageMapper.selectById(imageId);
        if (image == null) {
            return null;
        }
        return Paths.get(fileUtil.getImageDir(), image.getThumbPath());
    }

    /**
     * 删除商品图片（原图 + 缩略图 + 软删记录）
     */
    public Result<?> deleteImage(Integer imageId) {
        GoodsImage image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.error("图片不存在");
        }
        deleteFile(image.getRelativePath(), image.getThumbPath());

        image.setDeletedAt(LocalDateTime.now());
        imageMapper.updateById(image);
        return Result.success();
    }

    /**
     * 获取供应商图片路径
     */
    public Path getSupplierImagePath(Integer imageId) {
        SupplierImage image = supplierImageMapper.selectById(imageId);
        if (image == null) {
            return null;
        }
        return Paths.get(fileUtil.getImageDir(), image.getRelativePath());
    }

    /**
     * 获取供应商缩略图路径
     */
    public Path getSupplierThumbPath(Integer imageId) {
        SupplierImage image = supplierImageMapper.selectById(imageId);
        if (image == null) {
            return null;
        }
        return Paths.get(fileUtil.getImageDir(), image.getThumbPath());
    }

    /**
     * 删除供应商图片（原图 + 缩略图 + 软删记录）
     */
    public Result<?> deleteSupplierImage(Integer imageId) {
        SupplierImage image = supplierImageMapper.selectById(imageId);
        if (image == null) {
            return Result.error("图片不存在");
        }
        deleteFile(image.getRelativePath(), image.getThumbPath());

        image.setDeletedAt(LocalDateTime.now());
        supplierImageMapper.updateById(image);
        return Result.success();
    }

    private void deleteFile(String relativePath, String thumbRelativePath) {
        try {
            Files.deleteIfExists(Paths.get(fileUtil.getImageDir(), relativePath));
        } catch (IOException e) {
            // 忽略删除失败
        }
        if (thumbRelativePath != null) {
            try {
                Files.deleteIfExists(Paths.get(fileUtil.getImageDir(), thumbRelativePath));
            } catch (IOException e) {
                // 忽略删除失败
            }
        }
    }
}