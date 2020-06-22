package src.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;


public class FileHandler {
    /*
    This class implements application's file handler
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


    public byte[][] breakFile(File f) throws IOException {

        String content = Files.readString(f.toPath(), StandardCharsets.US_ASCII);
        int size = (int) Math.ceil(content.length() / 500.0);
        byte[][] messagesByte = new byte[size][512];
        byte[] fileContent = Files.readAllBytes(f.toPath());
        for (int i = 0; i < size; i++) {
            byte[] aux = (String.format("%04d", i) + String.format("%04d", size) + (getFileExtension(f)) + "CRC00000").getBytes();
            byte[] aux2 = Arrays.copyOfRange(fileContent, i * 500, Math.min((i + 1) * 500, fileContent.length));
            System.arraycopy(aux, 0, messagesByte[i], 0, aux.length);
            System.arraycopy(aux2, 0, messagesByte[i], aux.length, aux2.length);

        }

        return messagesByte;
    }

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

    public void mountFile(File[] file_chunks) {
        /*
        This method concatenates file chunks into a file
         */
    }

    public void mountFile(byte[][] messagesByte) {
        byte[] msg = new byte[500 * messagesByte.length];
        for (int i = 0; i < messagesByte.length; i++) {
            byte[] aux = Arrays.copyOfRange(messagesByte[i], 0, 4);

            System.out.println(new String(messagesByte[i]));
        }
    }

    public void validateFile(File f) {
        /*
        This method validates a file using shasum or md5sum
         */
    }
}
