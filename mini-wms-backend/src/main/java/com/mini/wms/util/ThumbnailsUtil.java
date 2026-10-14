package com.mini.wms.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 缩略图工具类
 */
@Component
public class ThumbnailsUtil {

    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 300;

    /**
     * 生成缩略图
     */
    public void generateThumbnail(Path sourcePath, Path thumbPath) throws IOException {
        ensureDirectory(thumbPath.getParent());
        Thumbnails.of(sourcePath.toFile())
                .size(THUMB_WIDTH, THUMB_HEIGHT)
                .outputQuality(0.85)
                .toFile(thumbPath.toFile());
    }

    /**
     * 生成缩略图（指定尺寸）
     */
    public void generateThumbnail(Path sourcePath, Path thumbPath, int width, int height) throws IOException {
        ensureDirectory(thumbPath.getParent());
        Thumbnails.of(sourcePath.toFile())
                .size(width, height)
                .outputQuality(0.85)
                .toFile(thumbPath.toFile());
    }

    private void ensureDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
