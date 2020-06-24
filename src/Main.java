package src;

import java.io.IOException;
import java.util.ArrayList;

import src.core.Client;
import src.core.FileHandler;
import src.core.Server;

public class Main {

    /*
     * This class implements main application
     */

    public static void main(String[] args) {
        Client c = new Client();
        Server s = new Server();
        new Thread(() -> {
            try {
                s.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        c.interact();
    }
}
