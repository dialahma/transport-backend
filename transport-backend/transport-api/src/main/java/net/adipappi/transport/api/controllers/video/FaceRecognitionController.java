package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.video.service.FaceRecognitionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private FaceRecognitionService faceRecognitionService;

    @PostMapping("/recognize")
    public ResponseEntity<String> recognizeFace(@RequestParam("file") MultipartFile file) {
        try {
            Mat image = convertToMat(file);
            return faceRecognitionService.recognize(image)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Visage non reconnu"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerFace(@RequestParam("label") String label,
                                               @RequestParam("file") MultipartFile file) {
        try {
            Mat image = convertToMat(file);
            faceRecognitionService.register(label, image);
            return ResponseEntity.ok("Face enregistrée pour " + label);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    private Mat convertToMat(MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        return net.adipappi.transport.video.util.FrameUtils.bufferedImageToMat(bufferedImage);
    }
}