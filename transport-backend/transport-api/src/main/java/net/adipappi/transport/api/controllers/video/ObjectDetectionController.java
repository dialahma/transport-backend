package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.DetectionService;
import net.adipappi.transport.video.service.ObjectDetectionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/api/detection")
public class ObjectDetectionController {
    @Autowired
    private DetectionService detectionService;

    @GetMapping(value = "/plate")
    public String detectionObjects(@RequestParam("file") MultipartFile file) {
        try {
            Mat image = convertToMat(file);
            return detectionService.detectionObjects(image);
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }


    private Mat convertToMat(MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        return net.adipappi.transport.video.util.FrameUtils.bufferedImageToMat(bufferedImage);
    }
}
