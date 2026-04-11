package com.phelim.system.love_certificate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class KeyLoaderUtil {
    public String loadPemFromResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        InputStream inputStream = resource.getInputStream();
        return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    }

}
