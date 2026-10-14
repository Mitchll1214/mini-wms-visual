package com.mini.wms.controller.supplier;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.SupplierImage;
import com.mini.wms.service.SupplierImageService;
import com.mini.wms.util.ImageUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 供应商资质图片控制器
 * 路径独立于 /api/supplier，避免与 /api/supplier/{id} 歧义
 */
@RestController
@RequestMapping("/api/supplier-images")
@RequiredArgsConstructor
public class SupplierImageController {

    private final SupplierImageService supplierImageService;
    private final ImageUploadUtil imageUploadUtil;

    /**
     * 上传供应商资质图片
     */
    @PostMapping("/{supplierId}/images")
    public Result<?> upload(@PathVariable Integer supplierId,
                            @RequestPart("file") MultipartFile file,
                            @RequestParam(defaultValue = "supplier") String imageType) throws IOException {
        SupplierImage image = imageUploadUtil.saveSupplierImage(file, supplierId, imageType);
        return Result.success(image);
    }

    /**
     * 资质图片列表
     */
    @GetMapping("/{supplierId}/images")
    public Result<?> list(@PathVariable Integer supplierId) {
        return supplierImageService.listBySupplier(supplierId);
    }

    /**
     * 原图
     */
    @GetMapping("/{imageId}/content")
    public ResponseEntity<Resource> content(@PathVariable Integer imageId) throws IOException {
        Path path = imageUploadUtil.getSupplierImagePath(imageId);
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new FileSystemResource(path.toFile()));
    }

    /**
     * 缩略图
     */
    @GetMapping("/{imageId}/thumb")
    public ResponseEntity<Resource> thumb(@PathVariable Integer imageId) throws IOException {
        Path path = imageUploadUtil.getSupplierThumbPath(imageId);
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new FileSystemResource(path.toFile()));
    }

    /**
     * 删除资质图片
     */
    @DeleteMapping("/{imageId}")
    public Result<?> delete(@PathVariable Integer imageId) {
        return imageUploadUtil.deleteSupplierImage(imageId);
    }
}