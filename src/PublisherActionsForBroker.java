import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class PublisherActionsForBroker extends Thread {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket requestSocket;
    private List<String> hashtagsPublished;
    private int numType;

    public PublisherActionsForBroker(Socket requestSocket,
                                     ObjectInputStream in,
                                     ObjectOutputStream out,
                                     List<String> hashtagsPublished, int numType) {

        this.requestSocket=requestSocket;
        this.in=in;
        this.out=out;
        this.hashtagsPublished=hashtagsPublished;
        this.numType = numType;
    }


    public void run() {


        System.out.println("PublisherThread started");
        try {
            String request = ((Message) in.readObject()).getString();
            System.out.println("The requested video is "+request);
            out.writeObject(new Message("Request Received"));

            VideoFile toreturn = new VideoFile(request, 0,numType);
            out.writeObject(new Message(toreturn.getVideoFileChunk(),toreturn.getChunkArray().length));
            for(int i=1; i<toreturn.getChunkArray().length; i++){
                byte[] array= toreturn.getVideo(i);
                out.writeObject(new Message(array,i));
            }

        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
        try {
            // closing resources
            this.requestSocket.close();
            this.in.close();
            this.out.close();
            System.out.println("Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}