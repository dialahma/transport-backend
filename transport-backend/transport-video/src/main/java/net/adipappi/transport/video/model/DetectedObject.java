package net.adipappi.transport.video.model;

import org.bytedeco.opencv.opencv_core.Rect;

public class DetectedObject {
    private final String label;
    private final float confidence;
    private final Rect boundingBox;

    public DetectedObject(String label, float confidence, Rect boundingBox) {
        this.label = label;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }
}

