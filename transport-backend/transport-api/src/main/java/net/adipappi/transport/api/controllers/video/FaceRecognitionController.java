package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.RecognitionService;
import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/api/faces")
public class FaceRecognitionController {

    @Autowired
    private RecognitionService recognitionService;


    @PostMapping("/recognize")
    public ResponseEntity<String> recognizeFace(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Mat image = FrameUtils.bufferedImageToMat(bufferedImage);
            return recognitionService.recognize(image)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerFace(
            @RequestParam("label") String label,
            @RequestParam("file") MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Mat image = FrameUtils.bufferedImageToMat(bufferedImage);
            recognitionService.register(label, image);
            return ResponseEntity.ok("Face registered successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
}