package net.adipappi.transport.video.util;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;


public class FrameUtils {
    private static final Java2DFrameConverter frameConverter = new Java2DFrameConverter();
    private static final OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();

    public static Mat frameToMat(Frame frame) {
        try {
            return matConverter.convert(frame);
        } catch (Exception e) {
            System.err.println("Error converting frame to Mat: " + e.getMessage());
            return new Mat();
        }
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_COLOR);
        } catch (Exception e) {
            System.err.println("Error converting BufferedImage to Mat: " + e.getMessage());
            return new Mat();
        }
    }

    public static Frame matToFrame(Mat mat) {
        try {
            return matConverter.convert(mat);
        } catch (Exception e) {
            System.err.println("Error converting Mat to Frame: " + e.getMessage());
            return null;
        }
    }
}