package src;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import src.core.Client;
import src.core.FileHandler;

public class ClientMain {

    /*
     * This class implements main application
     */

    public static void main(String[] args) {
        Scanner cmdLine = new Scanner(System.in);
        FileHandler fh = new FileHandler();
        Client c = new Client();

        System.out.print("Input file path:\n> ");
        String filePath = cmdLine.nextLine();
        cmdLine.close();

        File f = new File(filePath);

        try {
            c.slowStart(fh.breakFile(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
