package src;

import src.core.Client;
import src.core.FileHandler;

public class Main {

    /*
    This class implements main application
     */

    public static void main(String[]args) {

        FileHandler c = new FileHandler();
        byte[][] b = new byte[1][100];
        b[0] = "0001teste".getBytes();
        c.mountFile(b);
    }

}
