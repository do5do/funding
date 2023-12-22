package com.zerobase.funding.global.utils.s3;

import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;

import com.zerobase.funding.global.utils.s3.exception.S3Exception;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class AwsS3Utils {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Template s3Template;

    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + extension;

        ObjectMetadata objectMetadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            return s3Template.upload(bucketName, filename, inputStream, objectMetadata)
                    .getURL().toString();
        } catch (IOException e) {
            throw new S3Exception(INTERNAL_ERROR, "file upload to S3 failed.");
        }
    }

    public List<String> uploadFiles(List<MultipartFile> files) { // todo 병렬처리
        List<String> fileUrls = new ArrayList<>();
        files.forEach(file -> fileUrls.add(uploadFile(file)));
        return fileUrls;
    }
}
