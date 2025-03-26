package net.adipappi.transport.video.util;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class FrameUtils {
    private static final Java2DFrameConverter converter = new Java2DFrameConverter();

    public static BufferedImage frameToImage(Frame frame) {
        return converter.convert(frame);
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] imageInByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            Mat mat = opencv_imgcodecs.imdecode(new Mat(new BytePointer(imageInByte)), opencv_imgcodecs.IMREAD_COLOR);
            return mat;
        } catch (Exception e) {
            System.err.println("❌ Erreur de conversion BufferedImage -> Mat : " + e.getMessage());
            return new Mat();
        }
    }
}
