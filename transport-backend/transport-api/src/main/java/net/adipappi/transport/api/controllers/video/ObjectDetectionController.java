package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.DetectionService;
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
@RequestMapping("/api/detection")
public class ObjectDetectionController {
    @Autowired
    private DetectionService detectionService;

    @PostMapping("/objects")
    public ResponseEntity<String> detectObjects(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Mat image = FrameUtils.bufferedImageToMat(bufferedImage);
            String result = detectionService.detectionObjects(image);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing image: " + e.getMessage());
        }
    }
}