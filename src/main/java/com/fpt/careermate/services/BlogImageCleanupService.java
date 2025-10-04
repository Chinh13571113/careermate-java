package com.fpt.careermate.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogImageCleanupService {

    FileStorageService fileStorageService;

    /**
     * Extract image file names from HTML content
     */
    public List<String> extractImageFileNames(String htmlContent) {
        List<String> imageFileNames = new ArrayList<>();

        if (htmlContent == null || htmlContent.isEmpty()) {
            return imageFileNames;
        }

        // Pattern to match image sources in HTML
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String imageSrc = matcher.group(1);

            // Extract just the filename from the URL
            // Assuming URLs are like /api/files/{filename} or just {filename}
            String fileName = imageSrc.substring(imageSrc.lastIndexOf('/') + 1);
            imageFileNames.add(fileName);
        }

        return imageFileNames;
    }

    /**
     * Delete images that are no longer referenced in the blog content
     */
    public void cleanupUnusedImages(String oldContent, String newContent) {
        try {
            List<String> oldImages = extractImageFileNames(oldContent);
            List<String> newImages = extractImageFileNames(newContent);

            // Find images that were in old content but not in new content
            List<String> imagesToDelete = new ArrayList<>(oldImages);
            imagesToDelete.removeAll(newImages);

            // Delete unused images
            for (String fileName : imagesToDelete) {
                fileStorageService.deleteFile(fileName);
                log.info("Deleted unused image: {}", fileName);
            }

        } catch (Exception e) {
            log.error("Error during image cleanup: {}", e.getMessage());
        }
    }

    /**
     * Delete all images from a blog content (when blog is deleted)
     */
    public void deleteAllImagesFromContent(String htmlContent) {
        try {
            List<String> imageFileNames = extractImageFileNames(htmlContent);

            for (String fileName : imageFileNames) {
                fileStorageService.deleteFile(fileName);
                log.info("Deleted image from deleted blog: {}", fileName);
            }

        } catch (Exception e) {
            log.error("Error deleting images from blog content: {}", e.getMessage());
        }
    }

    /**
     * Scheduled task to clean up orphaned images (optional)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCleanup() {
        log.info("Starting scheduled image cleanup task");
        // This could be enhanced to check all images in storage
        // and compare with images referenced in active blogs
        // For now, it's just a placeholder for future enhancement
    }
}
