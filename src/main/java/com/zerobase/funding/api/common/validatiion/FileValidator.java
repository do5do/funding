package com.zerobase.funding.api.common.validatiion;

import static com.zerobase.funding.api.exception.ErrorCode.INTERNAL_ERROR;

import com.zerobase.funding.api.fundingproduct.exception.FundingProductException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.List;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    private final Tika tika = new Tika();
    private static final List<String> WHITE_LIST = List.of("image/jpeg", "image/tiff",
            "image/png", "image/gif", "image/bmp", "image/webp");

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        if (value.isEmpty()) {
            addMessage(context, "There is no file.");
            return false;
        }

        if (!StringUtils.hasText(value.getOriginalFilename())) {
            addMessage(context, "Filename does not exist.");
            return false;
        }

        String mimeType = getMimeType(value);
        return WHITE_LIST.stream().anyMatch(o -> o.equalsIgnoreCase(mimeType));
    }

    private String getMimeType(MultipartFile value) {
        try {
            return tika.detect(value.getInputStream());
        } catch (IOException e) {
            throw new FundingProductException(INTERNAL_ERROR, "get mime type failed");
        }
    }

    private void addMessage(ConstraintValidatorContext context, String msg) {
        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
    }
}
