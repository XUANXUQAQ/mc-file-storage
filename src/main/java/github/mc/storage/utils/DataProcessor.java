package github.mc.storage.utils;

import java.io.*;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

    /**
     * Decodes a Base64 string into a byte array.
     *
     * @param base64Data The Base64 encoded string.
     * @return The decoded byte array.
     */
    public static byte[] decodeFromBase64(String base64Data) {
        return Base64.getDecoder().decode(base64Data);
    }

    /**
     * Unzips a byte array (ZIP data) to a target directory.
     *
     * @param zippedData The ZIP data as a byte array.
     * @param targetDir  The target directory to extract files to.
     * @throws IOException If an I/O error occurs.
     */
    public static void unzipToDirectory(byte[] zippedData, Path targetDir) throws IOException {
        // 确保目标目录存在
        File targetDirFile = targetDir.toFile();
        if (!targetDirFile.exists()) {
            if (!targetDirFile.mkdirs()) {
                throw new IOException("Failed to create target directory: " + targetDir);
            }
        }

        // 解压 ZIP 文件
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName());

                // 安全检查:防止 Zip Slip 攻击
                if (!filePath.normalize().startsWith(targetDir.normalize())) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    File dir = filePath.toFile();
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Failed to create directory: " + filePath);
                    }
                } else {
                    // 确保父目录存在
                    File parentDir = filePath.getParent().toFile();
                    if (!parentDir.exists() && !parentDir.mkdirs()) {
                        throw new IOException("Failed to create parent directory: " + parentDir);
                    }

                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 从 Base64 数据解码并解压到目标目录
     * 这是一个便捷方法,结合了 decodeFromBase64 和 unzipToDirectory
     *
     * @param base64Data The Base64 encoded string.
     * @param targetDir  The target directory to extract files to.
     * @throws IOException If an I/O error occurs.
     */
    public static void restoreFilesFromBase64(String base64Data, Path targetDir) throws IOException {
        byte[] zippedData = decodeFromBase64(base64Data);
        unzipToDirectory(zippedData, targetDir);
    }
}