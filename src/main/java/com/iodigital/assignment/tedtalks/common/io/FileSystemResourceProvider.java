package com.iodigital.assignment.tedtalks.common.io;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FileSystemResourceProvider {

    public InputStream getInputStream(String filePath) throws IOException {
        return new FileSystemResource(filePath).getInputStream();
    }
}
