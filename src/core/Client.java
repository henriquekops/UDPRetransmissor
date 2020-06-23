package src.core;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
    /*
     * This class implements application's client
     */

    private DatagramSocket socket;
    private byte[] buffer;
    private InetAddress hostIP;

    private final int hostPort = 3000;
    private final int bufferSize = 1024;
    private final int maxRetries = 5;

    public Client() {
        /*
         * Constructor
         */

        System.out.println("Starting client...");

        try {
            this.hostIP = InetAddress.getByName("localhost");
            this.socket = new DatagramSocket();
            this.buffer = new byte[this.bufferSize];
        } catch (UnknownHostException error) {
            System.out.println("Unknown host exception: " + error.getMessage());
        } catch (SocketException error) {
            System.out.println("An error occurred while building socket: " + error.getMessage());
        }
    }

    public void interact() {
        /*
         * This method reads a file path from cmd line And breaks it into datagrams
         */
        Scanner cmdLine = new Scanner(System.in);
        System.out.print("Input file path:\n> ");
        String filePath = cmdLine.nextLine();

        File f = new File(filePath);
        FileHandler fh = new FileHandler();

        try {
            slowStart(fh.breakFile(f));
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public void slowStart(byte[][] datagrams) {
        /*
         * This method implements slow start technique
         */

        for (int i = 0; i < datagrams.length; i++) {
            continue;
        }
    }

    public void fastRetransmit() {
        /*
         * This method implements fast retransmit technique
         */
    }

    public int sendUntilAck(byte[] data, int waitTime) throws IOException {
        /*
         * This method implements timeout control
         */

        DatagramPacket getAck = new DatagramPacket(this.buffer, this.buffer.length);
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostIP, this.hostPort);

        int numTry = 1;
        boolean receivedAck = false;
        this.socket.setSoTimeout(waitTime);

        this.socket.send(sendPacket);

        while ((numTry <= this.maxRetries) && (!receivedAck)) {
            try {
                this.socket.receive(getAck);
                receivedAck = true;
            } catch (SocketTimeoutException error) {
                this.socket.send(sendPacket);
                numTry++;
                continue;
            }
        }

        if (numTry == this.maxRetries) {
            return -1;
        }
        else {
            ByteBuffer wrapper = ByteBuffer.wrap(getAck.getData());
            return wrapper.getShort();
        }
    }
}
