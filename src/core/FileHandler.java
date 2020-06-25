package src.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class FileHandler {
    /*
     * This class implements application's file handler
     */

    File f;

    public FileHandler() {
    }

    // Divide o arq em pedacos dentro de um array de bytes
    public byte[][] breakFile(File f) throws IOException {

        String content = Files.readString(f.toPath(), StandardCharsets.ISO_8859_1);
        int size = (int) Math.ceil(content.length() / 492.0);
        System.out.println(size);
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

        if (data.length != 492) {
            byte[] aux = new byte[492];
            System.arraycopy(data, 0, aux, 0, data.length);
            data = aux;
        }

        CRC32 crc32 = new CRC32();
        crc32.update(data);
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
        /* This method concatenates file chunks into a file */
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
        Path p = Paths.get("output." + extension);
        Files.write(p, fileBytes);
    }

    public void validateFile(File in, File out) {
        /*
         * This method validates a file using shasum or md5sum
         */

        try {
            //Use MD5 algorithm
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");

            //Get the checksum
            String checksumIn = getFileChecksum(md5Digest, in);
            String checksumOut = getFileChecksum(md5Digest, out);

            //see checksum
            if (checksumIn.equals(checksumOut)) {
                System.out.println("Arquivos iguais!");
            } else {
                System.out.println("Arquivos diferentes!");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        ;

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
