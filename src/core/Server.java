package src.core;

import java.io.*;
import java.net.*;
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

        this.log("Starting server...");

        try {
            this.lastAck = 0;
            this.buffer = new byte[512];
            this.completed = false;
            this.socket = new DatagramSocket(3000);
        } catch (SocketException error) {
            this.log("An error occurred while starting server: " + error.getMessage());
        }
    }

    public void listen() throws IOException {
        /*
         * This method listens to client connections and
         *  receives every file datagram to mount them
         */

        this.log("Listening...");

        while (!this.completed) {
            DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
            try {
                this.socket.receive(packet);
                this.handlePacket(packet);
            } catch (IOException error) {
                this.log("An error occurred wile receiving file: " + error.getMessage());
            }
        }

        this.log("File received!");
        FileHandler fileHandler = new FileHandler();
        fileHandler.mountFile(this.receivedData, extension);
    }

    private void handlePacket(DatagramPacket packet) {
        /*
         * This method handles a received packet
         * byte[] data = (4 bytes pos) + (4bytes tam) + (4 bytes ext) + (8 bytes CRC) + (data + padding);
         */

        byte[] data = packet.getData();
        int seqNumber = Integer.parseInt(new String(Arrays.copyOfRange(data, 0, 4)));
        int size = Integer.parseInt(new String(Arrays.copyOfRange(data, 4, 8)));
        String extension = new String(Arrays.copyOfRange(data, 8, 12));
        byte[] crc = Arrays.copyOfRange(data, 12, 20);

        this.log("Ack received: " + seqNumber + " | Last ack: " + this.lastAck);

        if (this.verifyCRC(data, crc)) {
            if (seqNumber == 0) {
                this.confirmedPackets = new boolean[size];
                this.ackCount = size;
                this.receivedData = new byte[size][492];
            }

            System.arraycopy(data, 20, this.receivedData[seqNumber], 0, data.length - 20);

            this.ackCount -= this.confirmedPackets[seqNumber] ? 0 : 1;
            this.confirmedPackets[seqNumber] = true;

            for (int i = 0; i < confirmedPackets.length; i++) {
                if (!confirmedPackets[i]) {
                    this.lastAck = i;
                    break;
                }
            }

            if (this.ackCount <= 0) {
                this.extension = extension;
                this.completed = true;
            }
        } else {
            this.log("Error on CRC(" + Arrays.toString(crc) + ") for seqNumber=" + seqNumber);
        }
        this.sendAck(packet.getAddress(), packet.getPort());
    }

    private boolean verifyCRC(byte[] data, byte[] crc) {
        /*
         * This method verifies data corruption through CRC
         */

        CRC32 crc32 = new CRC32();
        crc32.update(Arrays.copyOfRange(data, 20, data.length));
        long val = crc32.getValue();
        long aux = this.bytesToLong(crc);

        return val == aux;
    }

    private long bytesToLong(final byte[] bytes) {
        /*
         * This method converts a byte array to long integer
         */

        long result = 0;
        int offset = 0;
        for (int i = offset; i < Long.BYTES + offset; i++) {
            result <<= Long.BYTES;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }

    private void sendAck(InetAddress addressIP, int port) {
        /*
         * This method sends an acknowledgment to the client
         */

        byte[] ack = (completed ? (confirmedPackets.length + "") : (lastAck + "")).getBytes();

        try {
            DatagramPacket sendPacket = new DatagramPacket(ack, ack.length, addressIP, port);
            this.socket.send(sendPacket);
        } catch (IOException error) {
            this.log("Could not send ACK(" + lastAck + ") to " + addressIP + ":" + port);
        }

        if (completed) {
            this.endServer(addressIP, port);
        }
    }

    private void endServer(InetAddress addressIP, int port) {
        /*
         * This method ends the connection
         */

        this.log("Closing established connections...");
        DatagramPacket getAck = new DatagramPacket(this.buffer, this.buffer.length);

        try {
            this.socket.setSoTimeout(300);
            this.socket.receive(getAck);
            String data = new String(getAck.getData());

            if (data.equals("end")) {
                socket.close();
            } else {
                this.sendAck(addressIP, port);
            }

        } catch (SocketException e) {
            this.log("No acks received, exiting...");
        } catch (IOException e) {
            this.log("Error while reading packet: " + e.getMessage());
        }

    }

    private void log(String message) {
        /*
         * This method standardize logging style
         */

        System.out.print("[SERVER] ");
        System.out.println(message);
    }
}
