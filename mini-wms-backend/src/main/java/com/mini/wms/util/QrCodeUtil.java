package com.mini.wms.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

/**
 * 二维码生成工具类
 */
@Component
public class QrCodeUtil {

    private static final int QR_WIDTH = 256;
    private static final int QR_HEIGHT = 256;

    /**
     * 生成二维码 Base64 字符串
     */
    public String generateBase64(String content) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

            BufferedImage image = new BufferedImage(QR_WIDTH, QR_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, QR_WIDTH, QR_HEIGHT);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < QR_WIDTH; x++) {
                for (int y = 0; y < QR_HEIGHT; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // 转为 Base64
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }
}