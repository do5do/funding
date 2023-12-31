package com.zerobase.funding.api.s3.service;

import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;

import com.zerobase.funding.api.s3.dto.S3FileDto;
import com.zerobase.funding.api.s3.exception.AwsS3Exception;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsS3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Template s3Template;

    @Async
    public CompletableFuture<S3FileDto> uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + extension;

        ObjectMetadata objectMetadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            return CompletableFuture.completedFuture(new S3FileDto(
                    s3Template.upload(bucketName, filename, inputStream, objectMetadata)
                            .getURL().toString(), filename));
        } catch (IOException e) {
            log.error("IOException is occurred. ", e);
            throw new AwsS3Exception(INTERNAL_ERROR, "file upload to S3 failed.");
        }
    }

    @Async
    public void deleteFile(String filename) {
        try {
            s3Template.deleteObject(bucketName, filename);
        } catch (S3Exception e) {
            log.error("S3Exception is occurred. ", e);
            throw new AwsS3Exception(INTERNAL_ERROR, e.getMessage());
        }
    }
}
