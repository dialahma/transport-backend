package net.adipappi.transport.video.service;

import org.bytedeco.opencv.opencv_core.Mat;

public interface VideoDetectionService {

    /**
     * Analyse une image OpenCV et retourne une nouvelle image annotée.
     * @param frame L'image originale (format OpenCV Mat).
     * @return Une image annotée, ou une copie de l'image d'entrée en cas d'erreur.
     */
    Mat detectAndAnnotate(Mat frame);

    /**
     * Retourne le nom de la méthode de détection.
     * @return Une chaîne décrivant le type de détection (ex: "face", "vehicle", "plate").
     */
    String getName();

    /**
     * Permet une éventuelle réinitialisation du modèle (optionnel).
     */
    default void reset() {
        // Optionnel : à implémenter si le service a besoin de recharger un modèle
    }
}