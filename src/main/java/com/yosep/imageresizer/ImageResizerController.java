package com.yosep.imageresizer;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class ImageResizerController {

    @PostMapping("/resize")
    public ResponseEntity<BufferedImage> resize(InputStream dataStream, @RequestHeader(HttpHeaders.CONTENT_TYPE) String mimeType) throws IOException {
        BufferedImage sourceImage = ImageIO.read(dataStream);

        int newWidth = (int) (sourceImage.getWidth() * 0.5);
        int newHeight = (int) (sourceImage.getHeight() * 0.5);
        BufferedImage resizedImage = getScaledImage(sourceImage, newWidth, newHeight);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));

        return new ResponseEntity<>(resizedImage, headers, HttpStatus.CREATED);
    }

    @PostMapping("compress")
    public ResponseEntity<BufferedImage> compress(InputStream dataStream, @RequestHeader(HttpHeaders.CONTENT_TYPE) String mimeType) throws IOException {
        BufferedImage sourceImage = ImageIO.read(dataStream);

        float compressValue = 0.5F;

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();

        try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed)) {

            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("JPEG").next();

            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

            //to handle image png to get same output as original file with smaller size
            if (mimeType.contains("jpeg")) {

                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(compressValue);
                jpgWriter.setOutput(outputStream);
                jpgWriter.write(null, new IIOImage(sourceImage, null, null), jpgWriteParam);
                jpgWriter.dispose();

            } else {
                BufferedImage newBufferedImage = new BufferedImage(
                        sourceImage.getWidth(),
                        sourceImage.getHeight(),
                        BufferedImage.TYPE_INT_BGR);

                newBufferedImage.createGraphics()
                        .drawImage(newBufferedImage, 0, 0, Color.white, null);

                Graphics2D g2d = newBufferedImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, newBufferedImage.getWidth(), newBufferedImage.getHeight());
                g2d.drawImage(sourceImage, 0, 0, null);
                g2d.dispose();

                newBufferedImage.flush();

                jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpgWriteParam.setCompressionQuality(compressValue);

                jpgWriter.setOutput(outputStream);
                jpgWriter.write(null, new IIOImage(newBufferedImage, null, null), jpgWriteParam);
                jpgWriter.dispose();


            }
        } catch (Exception ex) {
            new ResponseEntity<>(sourceImage, headers, HttpStatus.CREATED);
        }

        byte [] byteArray = compressed.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        return new ResponseEntity<>(ImageIO.read(inputStream), headers, HttpStatus.CREATED);
    }

    private BufferedImage getScaledImage(BufferedImage src, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int x, y;
        int ww = src.getWidth();
        int hh = src.getHeight();
        for (x = 0; x < w; x++) {
            for (y = 0; y < h; y++) {
                int col = src.getRGB(x * ww / w, y * hh / h);
                img.setRGB(x, y, col);
            }
        }
        return img;
    }
}
