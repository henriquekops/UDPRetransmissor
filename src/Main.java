package src;

import java.util.ArrayList;

import src.core.Client;
import src.core.FileHandler;

public class Main {

    /*
     * This class implements main application
     */

    public static void main(String[]args) {
        /*long l = 15564658468234L;
        System.out.println(longToBytes(l));
        FileHandler c = new FileHandler();
        byte[][] b = new byte[1][100];
        b[0] = "0001teste".getBytes();*/
        //c.mountFile(b);
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

}
