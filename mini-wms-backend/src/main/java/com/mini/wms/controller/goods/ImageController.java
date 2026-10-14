package com.mini.wms.controller.goods;

import com.mini.wms.dto.Result;
import com.mini.wms.entity.GoodsImage;
import com.mini.wms.service.ImageService;
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
 * 商品图片控制器
 * 处理图片上传、列表、下载、删除
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadUtil imageUploadUtil;
    private final ImageService imageService;

    /**
     * 上传商品图片
     */
    @PostMapping("/{goodsId}/images")
    public Result<?> upload(@PathVariable Integer goodsId,
                            @RequestPart("file") MultipartFile file,
                            @RequestParam(defaultValue = "goods") String imageType,
                            @RequestParam(required = false) Integer binId) throws IOException {
        GoodsImage image = imageUploadUtil.saveImage(file, goodsId, imageType, binId);
        return Result.success(image);
    }

    /**
     * 获取图片列表
     */
    @GetMapping("/{goodsId}/images")
    public Result<?> list(@PathVariable Integer goodsId,
                          @RequestParam(defaultValue = "goods") String imageType) {
        return imageService.listByGoods(goodsId, imageType);
    }

    /**
     * 下载原图
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> download(@PathVariable Integer id) throws IOException {
        Path filePath = imageUploadUtil.getImagePath(id);
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    /**
     * 下载缩略图
     */
    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> downloadThumb(@PathVariable Integer id) throws IOException {
        Path thumbPath = imageUploadUtil.getThumbPath(id);
        if (thumbPath == null || !Files.exists(thumbPath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(thumbPath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return imageUploadUtil.deleteImage(id);
    }

    /**
     * 管理员图片管理：分页列表（商品图 + 供应商图）
     */
    @GetMapping("/admin/page")
    public Result<?> adminPage(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String imageType) {
        return imageService.adminPage(page, size, imageType);
    }
}