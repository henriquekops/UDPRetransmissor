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
    private int[] receivedAck;

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
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e);
        }
    }

    public void slowStart(byte[][] datagrams) throws InterruptedException {
        /*
         * This method implements slow start technique
         */
        receivedAck = new int[datagrams.length];
        int slowCount = -1;
        int status = -1;

        while (true) {

            int[] packetsToSend;
            if (status == -1) {
                slowCount = slowCount == -1 ? 1 : slowCount * 2;
                packetsToSend = getNextPackets(slowCount);
            } else {
                slowCount = 1;
                packetsToSend = new int[]{status};
                receivedAck[status] = 0;
            }

            if (packetsToSend == null) {
                break;
            }

            for (int i : packetsToSend) {
                try {
                    sendUntilAck(datagrams[i], 50); //tem que rever
                } catch (IOException error) {
                    System.out.println("Error: " + error);
                }
            }

            Thread.sleep(100);
            status = receiveAck();
        }
    }

    public int receiveAck() {
        return 0;
    }

    public int[] getNextPackets(int count) {
        for (int i = 0; i < receivedAck.length; i++) {
            if (receivedAck[i] == 0) {
                int end = Math.min(count, receivedAck.length - i);
                int[] aux = new int[end];
                for (int j = 0; j < end; j++) {
                    aux[j] = receivedAck[i + j];
                }
                return aux;
            }
        }
        return null;
    }

    public void fastRetransmit() {
        /*
         * This method implements fast retransmit technique
         */
    }


    public int sendUntilAck(byte[] data, int waitTime) throws IOException { //tem que mudar
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
        } else {
            ByteBuffer wrapper = ByteBuffer.wrap(getAck.getData());
            return wrapper.getShort();
        }
    }
}
