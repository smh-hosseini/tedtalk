package com.iodigital.assignment.tedtalks.common.io;

import com.iodigital.assignment.tedtalks.importcsv.exception.TedTalkImportException;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public class FileUtils {

    private FileUtils() {
    }

    public static String calculateStreamHash(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    digest.update(buffer);
                }
            }

            return HexFormat.of().formatHex(digest.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new TedTalkImportException("Failed to calculate stream hash", e);
        }
    }
}
