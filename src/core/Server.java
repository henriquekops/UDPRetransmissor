package src.core;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Server {
    /*
     * This class implements application's server
     */

    private DatagramSocket socket;
    private int lastAck;
    private byte[] buffer;
    private byte[][] receivedData;
    private boolean[] confirmedPackets;
    private int ackCount;
    private boolean completed;
    private String extension;

    public Server() {
        /*
         * Constructor
         */

        System.out.println("Starting server...");

        try {
            this.lastAck = 0;
            this.buffer = new byte[1024];
            this.completed = false;
            this.socket = new DatagramSocket(3000);
        } catch (SocketException error) {
            System.out.println("An error occurred while starting server: " + error.getMessage());
        }
    }

    public void listen() throws IOException {
        /*
         * This method receives a file from the client
         */

        System.out.println("Listening...");

        while (!completed) {
            DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);

            try {
                this.socket.receive(packet);
                new Thread(() -> {
                    this.handlePacket(packet);
                }).start();
            } catch (IOException error) {
                System.out.println("An error occurred wile receiving file: " + error.getMessage());
            }
        }
        FileHandler fh = new FileHandler();
        fh.mountFile(this.receivedData, extension);
    }

    public void handlePacket(DatagramPacket packet) {
        /*
         * This method handles a received packet
         * byte[] data = (4 bytes pos) + (4bytes tam) + (4 bytes extensao) + (8 bytes CRC) + (data + pading);
         */

        byte[] data = packet.getData();

        int seqNumber = Integer.parseInt(new String(Arrays.copyOfRange(data, 0, 4)));
        int size = Integer.parseInt(new String(Arrays.copyOfRange(data, 4, 8)));
        String extension = new String(Arrays.copyOfRange(data, 8, 12));
        byte[] crc = Arrays.copyOfRange(data, 12, 20);

        if (verifyCRC(data, crc)) {
            System.arraycopy(data, 20, this.receivedData[seqNumber], 0, data.length - 20);

            if (seqNumber == 0) {
                this.confirmedPackets = new boolean[size];
                this.ackCount = size;
            }

            if (this.lastAck + 1 == seqNumber) {
                for (int i = seqNumber + 1; i < confirmedPackets.length; i++) {
                    if (!confirmedPackets[i]) {
                        this.lastAck = i;
                        break;
                    }
                }
            }

            this.ackCount -= this.confirmedPackets[seqNumber] ? 0 : 1;
            this.confirmedPackets[seqNumber] = true;

            if (this.ackCount <= 0) {
                this.extension = extension;
                this.completed = true;
            }
        } else {
            System.out.println("Error on CRC(" + crc + ") for seqNumber=" + seqNumber);
        }
        this.sendAck(packet.getAddress(), packet.getPort());
    }

    public boolean verifyCRC(byte[] data, byte[] crc) {

        CRC32 crc32 = new CRC32();
        crc32.update(Arrays.copyOfRange(data, 20, data.length));
        long val = crc32.getValue();

        long aux = bytesToLong(crc, 0);

        if (val == aux) {
            return true;
        }

        return false;
    }

    public static long bytesToLong(final byte[] bytes, final int offset) {
        long result = 0;
        for (int i = offset; i < Long.BYTES + offset; i++) {
            result <<= Long.BYTES;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }

    public void sendAck(InetAddress addressIP, int port) {
        /*
         * This method sends an acknowledgment to the client
         */

        byte[] ack = (this.lastAck + "").getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(ack, ack.length, addressIP, port);
            this.socket.send(sendPacket);
        } catch (IOException error) {
            System.out.println("Could not send ACK(" + this.lastAck + ") to " + addressIP + ":" + port);
        }
    }

    public int getLastAck() {
        return lastAck;
    }

    public void setLastAck(int lastAck) {
        this.lastAck = lastAck;
    }
}
