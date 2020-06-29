package src.core;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {
    /*
     * This class implements application's client
     */

    private DatagramSocket socket;
    private byte[] buffer;
    private int[] receivedAck;
    private InetAddress hostIP;
    private final int hostPort = 3000;

    public Client() {
        /*
         * Constructor
         */

        this.log("Starting client...");

        try {
            this.hostIP = InetAddress.getByName("localhost");
            this.socket = new DatagramSocket();
            this.buffer = new byte[512];
        } catch (UnknownHostException error) {
            this.log("Unknown host exception: " + error.getMessage());
        } catch (SocketException error) {
            this.log("An error occurred while building socket: " + error.getMessage());
        }
    }

    public void slowStart(byte[][] datagrams) {
        /*
         * This method implements slow start technique
         */

        this.log("Slow start...");

        this.receivedAck = new int[datagrams.length];
        int slowCount = -1;
        int status = -1;

        while (true) {
            int[] packetsToSend;
            if (status == -1) {
                slowCount = slowCount == -1 ? 1 : Math.min(slowCount * 2, 4096);
                packetsToSend = this.getNextPackets(slowCount);
            } else {
                slowCount = 1;
                packetsToSend = new int[]{status};
                this.receivedAck[status] = 0;
            }

            if (packetsToSend == null) {
                break;
            }

            for (int i : packetsToSend) {
                try {
                    this.sendData(datagrams[i]);
                    this.log("Sending data " + i + "...");
                } catch (IOException e) {
                    this.log("Error occurred while sending data: " + e.getMessage());
                }
            }

            status = this.receiveAck();
            if (status == -2) {
                this.endClient();
                break;
            }
        }
        this.log("Upload completed!");
    }

    private int[] getNextPackets(int count) {
        /*
         * This method gets next packets to send to the server
         */

        for (int i = this.receivedAck.length - 1; i >= 0; i--) {
            if (this.receivedAck[i] != 0) {
                i++;
                int end = Math.min(count, this.receivedAck.length - i);
                int[] aux = new int[end];
                for (int j = 0; j < end; j++) {
                    aux[j] = i + j;
                }
                return aux;
            }
        }
        return this.receivedAck[0] == 0 ? new int[]{0} : null;
    }

    private int receiveAck() {
        /*
         * This method receives and interprets acknowledgements from the server
         */

        DatagramPacket getAck = new DatagramPacket(this.buffer, this.buffer.length);
        boolean retransmit = false;
        int nextAck = -1;

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
                this.receivedAck[ack - 1]++;

                if (this.receivedAck.length == ack) {
                    return -2;
                }
                if (this.receivedAck[ack - 1] == 3 && !retransmit) {
                    nextAck = ack;
                    retransmit = true;
                }
            }
        } catch (SocketException e) {
            this.log("Connection with server timed out, not receiving packets...");
        } catch (IOException e) {
            this.log("Error while receiving packets: " + e.getMessage());
        }

        if (retransmit) {
            this.log("Retransmitting " + nextAck);
        }
        return nextAck;
    }

    private void sendData(byte[] data) throws IOException {
        /*
         * This method sends data to the server
         */

        this.log("Sending data...");

        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostIP, this.hostPort);
        this.socket.send(sendPacket);
    }

    private void endClient() {
        /*
         * This method warns the server about the end of connection
         */

        byte[] data = "end".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostIP, this.hostPort);

        try {
            this.socket.send(sendPacket);
        } catch (IOException e) {
            this.log("Error while sending end packet " + e);
        }

        this.socket.close();
    }

    private void log(String message) {
        /*
         * This method standardize logging style
         */

        System.out.print("[CLIENTE] ");
        System.out.println(message);
    }
}
