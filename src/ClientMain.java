package src;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import src.core.Client;
import src.core.FileHandler;
import src.core.Server;

public class ClientMain {

    /*
     * This class implements main application
     */

    public static void main(String[] args) {
        Client c = new Client();
        c.interact();
    }
}
