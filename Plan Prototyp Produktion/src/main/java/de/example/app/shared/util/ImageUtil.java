package de.example.app.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Hilfskomponente zur Bildverarbeitung.
 * Skaliert hochgeladene Bilder auf eine konfigurierbare Maximalbreite (Standard: 1200 px)
 * unter Beibehaltung des Seitenverhältnisses und kodiert das Ergebnis als Base64-String
 * zur Speicherung in der Datenbank.
 */
@Component
public class ImageUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    @Value("${app.upload.max-width:1200}")
    private int maxWidth;

    /**
     * Verarbeitet eine hochgeladene Bilddatei: Liest das Bild ein, skaliert es bei Bedarf
     * auf die konfigurierte Maximalbreite und gibt es als Base64-kodierten String zurück.
     *
     * @param file die hochgeladene {@link MultipartFile}-Bilddatei
     * @return den Base64-kodierten String des verarbeiteten Bildes,
     *         oder {@code null} wenn die Datei leer ist, kein gültiges Bild enthält
     *         oder ein Fehler bei der Verarbeitung auftritt
     */
    public String processAndEncodeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                log.warn("Could not read image from MultipartFile. It might not be a valid image format.");
                return null;
            }

            BufferedImage resizedImage = resizeImage(originalImage, maxWidth);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = getFormatFromContentType(file.getContentType());
            if (format == null) {
                log.warn("Unsupported image content type: {}", file.getContentType());
                return null;
            }
            ImageIO.write(resizedImage, format, baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Error processing and encoding image: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Dekodiert einen Base64-kodierten Bild-String zurück in ein Byte-Array.
     *
     * @param base64Image der Base64-kodierte Bild-String
     * @return das dekodierte Byte-Array des Bildes,
     *         oder {@code null} wenn der String leer oder ungültig ist
     */
    public byte[] decodeImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            log.error("Error decoding Base64 image string: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Skaliert ein Bild proportional so, dass seine Breite die angegebene Maximalbreite
     * nicht überschreitet. Ist das Bild bereits schmaler als die Maximalbreite,
     * wird es unverändert zurückgegeben.
     *
     * @param originalImage das ursprüngliche {@link BufferedImage}
     * @param maxWidth      die maximale Breite in Pixeln, auf die das Bild skaliert werden soll
     * @return das skalierte {@link BufferedImage}, oder das Original wenn keine Skalierung nötig ist
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxWidth) {
            return originalImage;
        }

        double scaleFactor = (double) maxWidth / originalWidth;
        int newWidth = maxWidth;
        int newHeight = (int) (originalHeight * scaleFactor);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        log.debug("Resized image from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);
        return resizedImage;
    }

    /**
     * Ermittelt das Bildformat anhand des MIME-Typs der Datei.
     *
     * @param contentType der Content-Type-String der Datei (z. B. {@code "image/jpeg"})
     * @return das Formatbezeichner-Kürzel für {@link ImageIO} (z. B. {@code "jpeg"}, {@code "png"}),
     *         oder {@code null} wenn der Content-Type nicht unterstützt wird
     */
    private String getFormatFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpeg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/bmp" -> "bmp";
            case "image/webp" -> "webp";
            default -> {
                log.warn("Unsupported image content type: {}", contentType);
                yield null;
            }
        };
    }
}
