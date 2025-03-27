package net.adipappi.transport.service.video;
import net.adipappi.transport.video.service.FaceRecognitionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class RecognitionService {
    @Autowired
    private FaceRecognitionService faceRecognitionService;

    public List<String> recognizeFaces(Mat image) {
        // Implémentation...
        return faceRecognitionService.recognizeFaces(image);
    }

    public void register(String name, Mat faceImage) {
        faceRecognitionService.register(name, faceImage);
    }

    public Optional<String> recognize(Mat faceImage) {
        return faceRecognitionService.recognize(faceImage);
    }
}
