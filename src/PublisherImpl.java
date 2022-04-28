import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.*;
import java.util.List;

public class PublisherImpl implements Serializable  {

    private List<String> myHashTags;   // this is a list that contains the hashtags of this current Publisher
    private List<String> myVideoFiles;
    private List<VideoFile> videosConnectedWithHashtags;
    private int port;
    private int id;
    private String ip;
    private String channelName;
    private static final String type = "publisher";
    private int numType;

    public PublisherImpl(String ip,int port,ChannelName myChannel,int id) {
        this.ip =ip;
        this.port=port;
        this.id=id;
        this.numType=myChannel.type;
        this.myHashTags=myChannel.getHashTagsPublished();
        this.myVideoFiles=myChannel.getVideoNames();
        this.channelName = myChannel.getChannelName();
        this.videosConnectedWithHashtags = myChannel.getVideosConnectedWithHashtags();

    }

    public PublisherImpl(String ip,int port,List<String> hashtags,List<String> videoNames,String name,List<VideoFile> videosConnectedWithHashtags) {
        this.ip =ip;
        this.port=port;
        this.myHashTags=hashtags;
        this.myVideoFiles=videoNames;
        this.channelName= name;
        this.videosConnectedWithHashtags = videosConnectedWithHashtags;

    }

    public List<String> getMyVideoFiles() {

        return myVideoFiles;
    }

    public List<VideoFile> getVideosConnectedWithHashtags(){

        return videosConnectedWithHashtags;
    }

    public List<String> getMyHashTags(){

        return myHashTags;
    }

    public String getChannelName(){

        return channelName;
    }

    public int getPublisherPort() {

        return port;
    }

    public String getPublisherIP() {

        return ip;
    }

    public String getType(){

        return type;
    }

    private static String getSystemIP()
    {
        String current_ip = null;
        try(final DatagramSocket socket = new DatagramSocket())
        {
            socket.connect(InetAddress.getByName("1.1.1.1"), 10002);
            current_ip = socket.getLocalAddress().getHostAddress();
        }
        catch (SocketException | UnknownHostException e)
        {
            e.printStackTrace();
        }
        return current_ip;
    }

    public void addHashtag(String videoName,String addHashtag){

        for(VideoFile video: videosConnectedWithHashtags){
            if(video.getVideoName().equals(videoName))
                video.getAssociatedHashtags().add(addHashtag);
        }
        if(!myHashTags.contains(addHashtag))
            myHashTags.add(addHashtag);

        try {
            Socket connection = new Socket("127.0.0.1", 8000);
            Message request;
            Message typeOfUser;
            System.out.println("Connection with broker initialized\nBroker : " + connection);
            ObjectInputStream publisherIn = new ObjectInputStream(connection.getInputStream());
            ObjectOutputStream publisherOut = new ObjectOutputStream(connection.getOutputStream());

            System.out.println("Updating my info to the Broker System");
            typeOfUser = new Message(getType());
            publisherOut.writeObject(typeOfUser);
            request = new Message(ip, port, myHashTags, myVideoFiles,channelName,videosConnectedWithHashtags);
            publisherOut.writeObject(request);
            publisherOut.flush();

            Message reply = (Message) publisherIn.readObject();  // receive acknowledgement from broker
            System.out.println(reply.getString());

            publisherOut.writeObject(new Message("update"));
            publisherOut.flush();

            connection.close();
            publisherIn.close();
            publisherOut.close();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Publisher to Broker - Connection Problem");
        }
    }

    public void removeHashtag(String removeHashtag){
        for(VideoFile video: videosConnectedWithHashtags){
            if(video.getAssociatedHashtags().contains(removeHashtag))
                video.getAssociatedHashtags().remove(removeHashtag);
        }
        if(myHashTags.contains(removeHashtag))
            myHashTags.remove(removeHashtag);

        try {
            Socket connection = new Socket("127.0.0.1", 8000);
            Message request;
            Message typeOfUser;
            System.out.println("Connection with broker initialized\nBroker : " + connection);
            ObjectInputStream publisherIn = new ObjectInputStream(connection.getInputStream());
            ObjectOutputStream publisherOut = new ObjectOutputStream(connection.getOutputStream());

            System.out.println("Updating my info to the Broker System");
            typeOfUser = new Message(getType());
            publisherOut.writeObject(typeOfUser);
            request = new Message(ip, port, myHashTags, myVideoFiles,channelName,videosConnectedWithHashtags);
            publisherOut.writeObject(request);
            publisherOut.flush();

            Message reply = (Message) publisherIn.readObject();  // receive acknowledgement from broker
            System.out.println(reply.getString());

            publisherOut.writeObject(new Message("update"));
            publisherOut.flush();

            connection.close();
            publisherIn.close();
            publisherOut.close();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Publisher to Broker - Connection Problem");
        }
    }

    // Runs at initialization of the Publisher to get info for the BrokerList and each Broker's Hash
    public void init() {

        System.out.println("Publisher : " + id + " created and listens to port : " + port);

            try {
                Socket connection = new Socket(getSystemIP(), 6000);
                Message request;
                Message typeOfUser;
                System.out.println("Connection with broker initialized\nBroker : " + connection);
                ObjectInputStream publisherIn = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream publisherOut = new ObjectOutputStream(connection.getOutputStream());

                //      Sends the information(he's id,port and the hashtags that he is responsible for)
                //      of this current Publisher to first Broker.
                //      --NotifyBrokersForHashtags
                System.out.println("Sending my info to the Broker");
                typeOfUser = new Message(getType());
                publisherOut.writeObject(typeOfUser);
                request = new Message(ip, port, myHashTags, myVideoFiles,channelName,videosConnectedWithHashtags);
                publisherOut.writeObject(request);
                publisherOut.flush();

                Message reply = (Message) publisherIn.readObject();  // receive acknowledgement from broker
                System.out.println(reply.getString());

                publisherOut.writeObject(new Message("end connection"));
                publisherOut.flush();

                connection.close();
                publisherIn.close();
                publisherOut.close();

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Publisher to Broker - Connection Problem");
            }

            //      After I informed the Broker System about my hashtags
            //      I'm waiting for Broker requests.
            waitingForBrokerRequests();

    }


    public void waitingForBrokerRequests(){
        try {

            ServerSocket s = new ServerSocket(getPublisherPort());

            while(true) {

                Socket requestSocket;
                System.out.println("Waiting requests from Brokers");
                requestSocket = s.accept();
                ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
                Thread t = new PublisherActionsForBroker(requestSocket, in, out, myHashTags,numType);
                t.start();
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("Host not found / unknown host!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, TikaException {

        String typeOfcase = args[0];

        if(typeOfcase.equals("1")) {
            ChannelName userChannelName = new ChannelName("Gimli Snipes", 1);
            System.out.print("MY HASHTAGS --->");
            System.out.println(userChannelName.getHashTagsPublished().toString());
            System.out.print("MY VIDEOS --->");
            System.out.println(userChannelName.getVideoNames().toString());
            PublisherImpl p1 = new PublisherImpl(getSystemIP(), 6080, userChannelName, 1);
            p1.init();
        }else{
            ChannelName userChannelName = new ChannelName("Arwen Johnson", 2);
            System.out.print("MY HASHTAGS --->");
            System.out.println(userChannelName.getHashTagsPublished().toString());
            System.out.print("MY VIDEOS --->");
            System.out.println(userChannelName.getVideoNames().toString());
            PublisherImpl p2 = new PublisherImpl(getSystemIP(), 6090, userChannelName, 1);
            p2.init();
        }

    }

}