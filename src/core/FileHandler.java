package src.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class FileHandler {
    /*
     * This class implements application's file handler
     */

    public FileHandler() {
    }

    public byte[][] breakFile(File f) throws IOException {
        /*
         * This method breaks a file as an array of arrays of bytes
         */

        String content = Files.readString(f.toPath(), StandardCharsets.ISO_8859_1);
        int size = (int) Math.ceil(content.length() / 492.0);
        byte[][] messagesByte = new byte[size][512];
        byte[] fileContent = Files.readAllBytes(f.toPath());

        for (int i = 0; i < size; i++) {
            byte[] aux = (String.format("%04d", i) + String.format("%04d", size) + this.getFileExtension(f)).getBytes();
            byte[] aux3 = Arrays.copyOfRange(fileContent, i * 492, Math.min((i + 1) * 492, fileContent.length));
            byte[] aux2 = createCRC(aux3);

            System.arraycopy(aux, 0, messagesByte[i], 0, aux.length);
            System.arraycopy(aux2, 0, messagesByte[i], aux.length, aux2.length);
            System.arraycopy(aux3, 0, messagesByte[i], aux.length + aux2.length, aux3.length);
        }

        return messagesByte;
    }

    private byte[] createCRC(byte[] data) {
        /*
         * This method creates a CRC value for file data
         */

        if (data.length != 492) {
            byte[] aux = new byte[492];
            System.arraycopy(data, 0, aux, 0, data.length);
            data = aux;
        }

        CRC32 crc32 = new CRC32();
        crc32.update(data);
        long val = crc32.getValue();

        return this.longToBytes(val);
    }

    private byte[] longToBytes(long l) {
        /*
         * This method casts a long integer to an array of bytes
         */

        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    private String getFileExtension(File f) {
        /*
         * This method extracts the file extension (maximum 4 chars)
         * and stores it inside breakFile's byte array
         */

        String extension = "";
        try {
            if (f != null && f.exists()) {
                String name = f.getName();
                extension = name.substring(name.lastIndexOf(".") + 1);

                if (extension.length() < 4) {
                    while (true) {
                        if (extension.length() != 4) {
                            extension = "0" + extension;
                        } else {
                            break;
                        }
                    }
                } else {
                    if (extension.length() > 4) {
                        throw new Exception();
                    }
                }
            }
        } catch (Exception e) {
            extension = "0txt";
        }
        return extension;
    }

    public void mountFile(byte[][] fileParts, String extension) throws IOException {
        /*
         * This method concatenates file chunks into a file
         */

        int padding = 0;
        for (int i = fileParts[0].length - 1; i >= 0; i--) {
            if (fileParts[fileParts.length - 1][i] != 0) {
                padding = i + 1;
                break;
            }
        }

        byte[] fileBytes = new byte[(fileParts.length * fileParts[0].length) - (492 - padding)];
        for (int i = 0; i < fileParts.length - 1; i++) {
            System.arraycopy(fileParts[i], 0, fileBytes, i * fileParts[i].length, fileParts[i].length);
        }

        System.arraycopy(fileParts[fileParts.length - 1], 0, fileBytes, (fileParts.length - 1) * fileParts[0].length, padding);

        extension = extension.substring(extension.lastIndexOf("0") + 1);
        Path p = Paths.get("tmp/output." + extension);
        Files.write(p, fileBytes);
        this.log("File saved at " + p.toString());
    }

    public void validateFile(File in, File out) {
        /*
         * This method validates a file using MD5
         */

        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");

            String checksumIn = this.getFileChecksum(md5Digest, in);
            String checksumOut = this.getFileChecksum(md5Digest, out);

            if (checksumIn.equals(checksumOut)) {
                this.log("Files are identic! :)");
            } else {
                this.log("Files are different... :(");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            this.log("Error: " + e);
        }
    }

    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        /*
         * This method generates a hash over file's byte array
         * for checksum calculation
         */

        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public void log(String message) {
        /*
         * This method standardize logging style
         */

        System.out.print("[FILE_HANDLER] ");
        System.out.println(message);
    }

}
