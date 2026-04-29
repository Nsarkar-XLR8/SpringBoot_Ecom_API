package com.ecommerce.shop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.shop.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new BusinessException("Failed to upload image to Cloudinary.");
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Cloudinary delete failed for URL: {}", imageUrl, e);
        }
    }

    private String extractPublicId(String url) {
        // Simple extraction for standard Cloudinary URLs
        // Format: https://res.cloudinary.com/cloud_name/image/upload/v1234567/public_id.jpg
        try {
            String[] parts = url.split("/");
            String fileName = parts[parts.length - 1];
            return fileName.substring(0, fileName.lastIndexOf("."));
        } catch (Exception e) {
            log.warn("Could not extract publicId from URL: {}", url);
            return null;
        }
    }
}
