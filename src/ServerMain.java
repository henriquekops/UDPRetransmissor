package src;

import src.core.FileHandler;
import src.core.Server;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ServerMain {

    public static void main(String[] args) {
        Scanner cmdLine = new Scanner(System.in);
        FileHandler fh = new FileHandler();
        Server s = new Server();

        try {
            s.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("Input first file path:\n> ");
        String first = cmdLine.nextLine();

        System.out.print("Input second file path:\n> ");
        String second = cmdLine.nextLine();
        cmdLine.close();

        File in = new File(first);
        File out = new File(second);

        fh.validateFile(in, out);
    }
}
