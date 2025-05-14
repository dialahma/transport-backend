package net.adipappi.transport.video.util;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class FrameUtils {
    private static final Logger logger = LoggerFactory.getLogger(FrameUtils.class);
    private static final Java2DFrameConverter frameConverter = new Java2DFrameConverter();

    public static Mat frameToMat(Frame frame) {
        try {
            BufferedImage image = frameConverter.convert(frame);
            return bufferedImageToMat(image);
        } catch (Exception e) {
            logger.error("Frame to Mat conversion error", e);
            return new Mat();
        }
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return opencv_imgcodecs.imdecode(new Mat(new BytePointer(bytes)),
                    opencv_imgcodecs.IMREAD_COLOR);
        } catch (Exception e) {
            logger.error("BufferedImage to Mat conversion error", e);
            return new Mat();
        }
    }

    public static Frame matToFrame(Mat mat) throws IOException {
        if (mat.empty()) {
            throw new IOException("Cannot convert empty Mat to Frame");
        }

        BytePointer bp = new BytePointer();
        if (!opencv_imgcodecs.imencode(".jpg", mat, bp)) {
            throw new IOException("Mat encoding failed");
        }

        byte[] bytes = new byte[(int)bp.limit()];
        bp.get(bytes);
        return frameConverter.convert(ImageIO.read(new ByteArrayInputStream(bytes)));
    }
}