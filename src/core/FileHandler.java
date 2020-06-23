package src.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.CRC32;

public class FileHandler {
    /*
     * This class implements application's file handler
     */

    File f;
    File[] file_chunks;

    public FileHandler() {
        f = this.f;
        file_chunks = this.file_chunks;
    }

    public File readFile(String filePath) {
        f = new File(filePath);
        return f;
    }

    // Divide o arq em pedacos dentro de um array de bytes
    public byte[][] breakFile(File f) throws IOException {

        String content = Files.readString(f.toPath(), StandardCharsets.US_ASCII);
        int size = (int) Math.ceil(content.length() / 492.0);
        byte[][] messagesByte = new byte[size][512];
        byte[] fileContent = Files.readAllBytes(f.toPath());

        for (int i = 0; i < size; i++) {
            byte[] aux = (String.format("%04d", i) + String.format("%04d", size) + getFileExtension(f)).getBytes();
            byte[] aux3 = Arrays.copyOfRange(fileContent, i * 492, Math.min((i + 1) * 492, fileContent.length));
            byte[] aux2 = verifyCRC(aux3);
            System.arraycopy(aux, 0, messagesByte[i], 0, aux.length);
            System.arraycopy(aux2, 0, messagesByte[i], aux.length, aux2.length);
            System.arraycopy(aux3, 0, messagesByte[i], aux.length + aux2.length, aux3.length);

        }

        return messagesByte;
    }

    public byte[] verifyCRC(byte[] data) {

        CRC32 crc32 = new CRC32();
        crc32.update(Arrays.copyOfRange(data, 20, data.length));
        long val = crc32.getValue();

        return longToBytes(val);
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    // Metodo de extracao da extensao de um arquivo(com no maximo 4 chars),
    // utilizado exclusivamente por breakFile para guardar no array criado
    public String getFileExtension(File f) {
        String extension = "";
        try {
            if (f != null && f.exists()) {
                String name = f.getName();
                extension = name.substring(name.lastIndexOf("."));
                extension = extension.substring(1);
                if (extension.length() != 4) {
                    while (true) {
                        if (extension.length() != 4) {
                            extension = "0" + extension;
                        }
                    }
                }
            }
        } catch (Exception e) {
            extension = "";
        }
        return extension;
    }

    public void mountFile(byte[][] fileParts, String extension) throws IOException {
        /* This method concatenates file chunks into a file */
        byte[] fileBytes = new byte[fileParts.length * fileParts[0].length];
        for (int i = 0; i < fileParts.length; i++) {
            System.arraycopy(fileParts[i], 0, fileBytes, i * fileParts[i].length, fileParts[i].length);
        }
        Path p = Paths.get("C:\\Arquivo." + extension);
        Files.write(p, fileBytes);
    }

    public void validateFile(File f) {
        /*
         * This method validates a file using shasum or md5sum
         */
    }
}
