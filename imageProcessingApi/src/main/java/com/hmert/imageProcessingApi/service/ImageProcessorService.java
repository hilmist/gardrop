package com.hmert.imageProcessingApi.service;

import com.hmert.imageProcessingApi.dto.ProcessResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

@Service
public class ImageProcessorService {
    private static final int MAX_WIDTH = 720;
    private static final int MAX_HEIGHT = 1280;


    public ProcessResult process(MultipartFile multipartFile, String destinationFilePath) {
        try {
            BufferedImage inputImage = ImageIO.read(multipartFile.getInputStream());
            // null kontrolü
            if (inputImage == null) {
                throw new IllegalArgumentException("Yüklenen dosya geçerli bir görsel formatında değil.");
            }
            // Boyutlandırma
            Dimension newSize = resizeKeepingAspect(inputImage.getWidth(), inputImage.getHeight());
            BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(inputImage, 0, 0, newSize.width, newSize.height, null);
            g.dispose();

            // Hedef path
            Path outputPath = Path.of("images");
            Files.createDirectories(outputPath);
            File outputFile = outputPath.resolve(destinationFilePath).toFile();

            // %90 kalite ile resmi yeniden oluşturma
            try (OutputStream os = new FileOutputStream(outputFile)) {
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = writers.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.9f); // %90 kalite
                }

                writer.write(null, new IIOImage(resizedImage, null, null), param);
                ios.close();
                writer.dispose();
            }

            return new ProcessResult(
                    destinationFilePath.replace(".jpg", ""),
                    outputFile.getAbsolutePath(),
                    newSize.width,
                    newSize.height,
                    "success"
            );

        } catch (IOException e) {
            throw new RuntimeException("Image processing failed", e);
        }
    }

    private Dimension resizeKeepingAspect(int width, int height) {
        double widthRatio = (double) MAX_WIDTH / width;
        double heightRatio = (double) MAX_HEIGHT / height;
        double scale = Math.min(1.0, Math.min(widthRatio, heightRatio));
        return new Dimension((int) (width * scale), (int) (height * scale));
    }
}
