package src.core;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
        System.out.print("Input file path:\n> test.txt\n");
        //String filePath = cmdLine.nextLine();

        File f = new File("README.md");
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
        System.out.println("Slow start...");
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
                    sendData(datagrams[i]); //tem que rever
                } catch (IOException error) {
                    System.out.println("Error: " + error);
                }
            }

            status = receiveAck();
            if (status == -2) {
                break;
            }
        }
        System.out.println("terminado");
    }

    public int[] getNextPackets(int count) {
        System.out.println("getNextPackets...");
        for (int i = receivedAck.length - 1; i >= 0; i--) {
            if (receivedAck[i] != 0) {
                i++;
                System.out.println("POSICAO " + i);
                int end = Math.min(count, receivedAck.length - i);
                int[] aux = new int[end];
                for (int j = 0; j < end; j++) {
                    aux[j] = i + j;
                }
                return aux;
            }
        }
        return receivedAck[0] == 0 ? new int[]{0} : null;
    }

    public int receiveAck() {
        System.out.println("receiveAck...");
        DatagramPacket getAck = new DatagramPacket(this.buffer, this.buffer.length);

        try {
            this.socket.setSoTimeout(100);
            while (true) {
                this.socket.receive(getAck);

                byte[] data = getAck.getData();
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == 0) {
                        data = Arrays.copyOfRange(data, 0, i);
                    }
                }

                int ack = Integer.parseInt(new String(data));
                System.out.println("ACK RECEBIDO " + ack);
                receivedAck[ack - 1]++;
                if (receivedAck.length == ack) {
                    return -2;
                }
                if (receivedAck[ack - 1] == 3) {
                    return ack;
                }
            }
        } catch (IOException e) {
            System.out.println("End receive acks");
        }
        return -1;
    }


    public void sendData(byte[] data) throws IOException {
        System.out.println("sendData...");
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostIP, this.hostPort);
        this.socket.send(sendPacket);
    }
}
