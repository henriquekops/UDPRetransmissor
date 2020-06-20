package src.core;

public class Server {
    /*
    This class implements application's server
     */

    private int seqNumber;

    public Server() {
        /*
        Constructor
         */
    }

    public void receiveFile() {
        /*
        This method receives a file from the client
         */
    }

    public void sendAck() {
        /*
        This method sends an acknowledgment to the client
         */
    }

    public void throwDataAway() {
        /*
        This method discards corrupted data
         */
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }
}
