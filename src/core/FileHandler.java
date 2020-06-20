package src.core;

import java.io.File;

public class FileHandler {
    /*
    This class implements application's file handler
     */

    public FileHandler() {
        /*
        Constructor
         */
    }

    public void readFile(String filePath) {
        /*
        This method reads a file from parametrized path
         */
    }

    public void breakFile(File f) {
        /*
        This method breaks a file into chunks of 512 bytes
         */
    }

    public void mountFile(File[] file_chunks) {
        /*
        This method concatenates file chunks into a file
         */
    }

    public void validateFile(File f) {
        /*
        This method validates a file using shasum or md5sum
         */
    }
}
