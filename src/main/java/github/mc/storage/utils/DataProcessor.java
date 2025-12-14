package github.mc.storage.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataProcessor {

    /**
     * Zips a list of files and returns the zipped content as a byte array.
     *
     * @param files The list of files to zip.
     * @return A byte array representing the zipped file content.
     * @throws IOException If an I/O error occurs.
     */
    public static Optional<byte[]> zipFiles(List<File> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return Optional.empty();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addDirectoryToZip(zos, file, file.getName());
                } else {
                    addFileToZip(zos, file, "");
                }
            }
        }
        return Optional.of(baos.toByteArray());
    }

    private static void addDirectoryToZip(ZipOutputStream zos, File folder, String parentPath) throws IOException {
        var subFiles = folder.listFiles();
        if (subFiles == null) {
            return;
        }
        for (File file : subFiles) {
            if (file.isDirectory()) {
                addDirectoryToZip(zos, file, parentPath + "/" + file.getName());
            } else {
                addFileToZip(zos, file, parentPath);
            }
        }
    }

    private static void addFileToZip(ZipOutputStream zos, File file, String parentPath) throws IOException {
        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(file)) {
            String entryName = parentPath.isEmpty() ? file.getName() : parentPath + "/" + file.getName();
            zos.putNextEntry(new ZipEntry(entryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }

    /**
     * Encodes a byte array into a Base64 string.
     *
     * @param data The byte array to encode.
     * @return The Base64 encoded string.
     */
    public static String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}