package com.amalitech.aws_project_lab_two.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amalitech.aws_project_lab_two.dto.ImageUploadRequest;
import com.amalitech.aws_project_lab_two.dto.S3ObjectInfo;
import com.amalitech.aws_project_lab_two.entity.Image;
import com.amalitech.aws_project_lab_two.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObjectsService {

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Transactional
    public String uploadObject(MultipartFile multipartFile, ImageUploadRequest request) throws MaxUploadSizeExceededException {
        try {
            File convertedFile = convertMultipartToFile(multipartFile);
            String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
            convertedFile.delete();

            String imageUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;

            Image image = Image.builder()
                    .name(fileName)
                    .url(imageUrl)
                    .description(request.getDescription())
                    .build();

            imageRepository.save(image);

            return "File uploaded successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public List<S3ObjectInfo> getAllObjects() {
        List<Image> images = imageRepository.findAll(Sort.by("createdAt").descending());

        return images.stream()
                .map(image -> new S3ObjectInfo(
                        image.getName(),
                        image.getUrl(),
                        image.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteObjectByName(String fileId) {
        try {
            amazonS3.deleteObject(bucketName, fileId);

            Image image = imageRepository.findByName(fileId);
            if (image != null) {
                imageRepository.delete(image);
            }

            return "File deleted successfully";
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
}