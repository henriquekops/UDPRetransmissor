package src.core;

public class Client {
    /*
    This class implements application's client
     */

    public Client() {
        /*
        Constructor
         */
    }

    public void slowStart() {
        /*
        This method implements slow start technique
         */
    }

    public void fastRetransmit() {
        /*
        This method implements fast retransmit technique
         */
    }

    public void timeoutControl() {
        /*
        This method implements timeout control
         */
    }
    
    public byte[][] divideMessage(String message){

        int size = (int) Math.ceil(message.length() / 508.0);
        byte[][] messagesByte = new byte[size][512];

        for (int i = 0; i < size; i++) {
            String aux = String.format("%04d", i);
            aux += message.substring(i * 508, Math.min((i + 1) * 508, message.length()));

            byte[] arr = aux.getBytes();
            System.arraycopy(arr, 0, messagesByte[0], 0, arr.length);
        }

        return messagesByte;
    }
}
