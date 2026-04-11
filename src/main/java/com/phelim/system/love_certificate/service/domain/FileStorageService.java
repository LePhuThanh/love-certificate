package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class FileStorageService {

    public String savePdfFile(String fileName, byte[] data) {

        try {
            Path path = Path.of(BaseConstants.BASE_PATH, fileName);

            Path parentDir = path.getParent();
            if (parentDir != null && Files.notExists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.write(path, data);

            // Convert to URL style
            return path.toString().replace("\\", "/");

        } catch (IOException ex) {
            log.error("[FileStorageService][save] File save failed", ex);
            throw new BusinessException(ErrorCode.SAVING_FILE_FAILED, null);
        }
    }
}
