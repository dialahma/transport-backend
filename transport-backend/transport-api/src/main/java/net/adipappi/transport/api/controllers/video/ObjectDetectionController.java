package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.DetectionService;
import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

@RestController
@RequestMapping("/api/detection")
public class ObjectDetectionController {

    private static final Logger logger = LoggerFactory.getLogger(ObjectDetectionController.class);

    @Autowired
    private DetectionService detectionService;

    @PostMapping("/object")
    public ResponseEntity<String> detectObject(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Reçu demande de détection - Taille fichier: {} bytes", file.getSize());
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Mat image = FrameUtils.bufferedImageToMat(bufferedImage);
            logger.debug("Image convertie en Mat: {}x{}", image.cols(), image.rows());

            String result = detectionService.detectionObjects(image);
            logger.info("Détection terminée: {}", result);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Erreur lors de la détection", e);
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }

    @PostMapping(value = "/objects", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> detectObjects(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Fichier vide");
            }
            logger.info("Reçu demande de détection - Taille fichier: {} bytes", file.getSize());

            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Mat image = FrameUtils.bufferedImageToMat(bufferedImage);
            logger.debug("Image convertie en Mat: {}x{}", image.cols(), image.rows());

            String result = detectionService.detectionObjects(image);
            logger.info("Détection terminée: {}", result);

            // Récupérer l'image annotée et la convertir en JPEG
            Mat annotatedImage = detectionService.getLastAnnotatedImage();
            byte[] imageBytes = FrameUtils.matToJpegBytes(annotatedImage);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Content-Disposition", "attachment; filename=result.jpg")
                    .body(imageBytes);

        } catch (Exception e) {
            logger.error("Erreur lors de la détection", e);
            return ResponseEntity.internalServerError()
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/{type}")
    public ResponseEntity<byte[]> detect(@PathVariable String type,
                                         @RequestBody byte[] imageBytes) {
        Mat input = imdecode(new Mat(new BytePointer(imageBytes)), IMREAD_COLOR);
        Mat result = detectionService.detect(type, input);

        BytePointer buffer = new BytePointer();
        imencode(".jpg", result, buffer);
        byte[] resultBytes = new byte[(int) buffer.limit()];
        buffer.get(resultBytes);
        return ResponseEntity.ok().body(resultBytes);
    }

    @GetMapping("/types")
    public List<String> available() {
        return detectionService.available();
    }
}