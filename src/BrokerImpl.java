import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.List;


public class BrokerImpl {

    private ServerSocket broker = null;
    private Socket connection = null ; // connection to client
    public String ip;
    public int port;
    public int id;
    ObjectOutputStream out;
    ObjectInputStream in;
    public ArrayList<PublisherImpl> registeredPublishers;
    public ArrayList<PublisherImpl> registeredPublishersBasedOnHashTag;
    public ArrayList<String> brokerHashtags;
    public BrokerImpl[] activeBrokers;
    private static String type = "broker";
    
    public BrokerImpl(String ip,int port, int id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.registeredPublishers = new ArrayList<>();
        this.registeredPublishersBasedOnHashTag = new ArrayList<>();
        this.brokerHashtags = new ArrayList<>();
        this.activeBrokers = new BrokerImpl[5];
    }

    public BrokerImpl(String ip,int port, int id,ArrayList<PublisherImpl> p,ArrayList<String> hashtags) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.registeredPublishers = p;
        this.brokerHashtags = hashtags;
    }

    public BrokerImpl(String ip,int port, int id,ArrayList<PublisherImpl> p,ArrayList<String> hashtags,ArrayList<PublisherImpl> registeredPublishersBasedOnHashTag) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.registeredPublishers = p;
        this.brokerHashtags = hashtags;
        this.registeredPublishersBasedOnHashTag = registeredPublishersBasedOnHashTag;
    }

    public double getBrokerHashNumber(){

        if(getId() == 1)
            return 30;
        else if(getId() == 2)
            return 60;
        else
            return 100;
    }

    public double getLowerBrokerHashNumber(){
        if(getId() == 1)
            return 0;
        else if(getId() == 2)
            return 30;
        else
            return 60;
    }

    public ArrayList<String> getBrokerHashTags() {

        return brokerHashtags;
    }

    public ArrayList<PublisherImpl> getRegisteredPublishersBasedOnHashtag() {
        return registeredPublishersBasedOnHashTag;
    }

    public void setBrokerHashtags(ArrayList<String> hashtags){
        this.brokerHashtags = hashtags;
    }


    public int getPort(){

        return this.port;
    }

    public ArrayList<PublisherImpl> getRegisteredPublishers(){

        return registeredPublishers;
    }


    public void setRegisteredPublishers(ArrayList<PublisherImpl> publishers){

        this.registeredPublishers = publishers;
    }

    public int getId(){

        return this.id;
    }

    public String getIP(){

        return this.ip;
    }
	
	
	public void init() {

		try
		{

            // main broker's socket
            broker = new ServerSocket(port);
			
			displayMessage( ConsoleColors.GREEN +"Broker "+this.getId() +" created and listening on port : \n" + broker.getLocalPort() +
                    "."+ConsoleColors.RED+"\nBrokers runs to accept connections from Publishers,Consumers and other Brokers."+ConsoleColors.RESET);

            Message message;
            Message typeOfUser;

			while (true)
            {

                displayMessage( "Waiting for connection . . ." );
                connection = broker.accept(); // allow broker to accept connection
                displayMessage( "Connection received from: " + connection.getInetAddress());

                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());

                typeOfUser = (Message)in.readObject();

                if((typeOfUser.getString()).equalsIgnoreCase("publisher")) {
                    message = (Message) in.readObject();
                    PublisherImpl newPub = new PublisherImpl(message.getIP(), message.getPort(), message.getListOfHashTags(),message.getVideoNames(),message.getChannelName(),message.getVideosConnectedWithHashtags());
                    System.out.println("Information about Publisher received. ");

                    message = new Message("Information received successfully!");
                    out.writeObject(message);

                    message = (Message)in.readObject();

                    closeConnection();

                    //if works ,to separate actions to be done on broker when a publisher is connecting for the first time or when he is just updating his hashtags
                    if(message.getString().equals("update")){

                        for(PublisherImpl publisher: registeredPublishers){
                            if(publisher.getChannelName().equals(newPub.getChannelName())) {
                                registeredPublishers.remove(publisher);
                                registeredPublishers.add(newPub);
                            }
                        }
                        for(PublisherImpl publisher: registeredPublishersBasedOnHashTag){
                            if(publisher.getChannelName().equals(newPub.getChannelName())) {
                                registeredPublishersBasedOnHashTag.remove(publisher);
                                registeredPublishersBasedOnHashTag.add(newPub);
                            }
                        }
                        brokerHashtags = null;
                        for(PublisherImpl publisher: registeredPublishersBasedOnHashTag){
                            for(String hashtag: publisher.getMyHashTags()){
                                if(!brokerHashtags.contains(hashtag)){
                                    brokerHashtags.add(hashtag);
                                }
                            }
                        }

                        notifyBrokersOnChanges(newPub,"update");

                    }else{
                        double newPubHash = getMd5(newPub.getChannelName());

                        if(newPubHash < getBrokerHashNumber()) {
                            registeredPublishers.add(newPub);
                            System.out.println("registeredPublishers.size() = " + registeredPublishers.size());
                        }
                        boolean flag = false;

                        for(String hashtag: newPub.getMyHashTags()){
                            if(getMd5(hashtag) < getBrokerHashNumber()){
                                flag = true;
                                if(!brokerHashtags.contains(hashtag)){
                                    brokerHashtags.add(hashtag);
                                }
                            }
                        }
                        if(flag=true){
                            registeredPublishersBasedOnHashTag.add(newPub);
                        }

                        System.out.println(getBrokerHashTags().toString());

                        // Inform other brokers on new publisher addition
                        notifyBrokersOnChanges(newPub,"newPub");
                    }

                }else if((typeOfUser.getString()).equalsIgnoreCase("consumer")){

                    displayMessage( "Connection received from: " + connection.toString());

                    acceptConnectionForConsumer();

                }else{

                    message = (Message)in.readObject();
                    String typeOf = message.getString();

                    message = (Message)in.readObject();
                    PublisherImpl newPub = new PublisherImpl(message.getIP(), message.getPort(), message.getListOfHashTags(),message.getVideoNames(),message.getChannelName(),message.getVideosConnectedWithHashtags());

                    if(typeOf.equals("update")){

                        for(PublisherImpl publisher: registeredPublishers){
                            if(publisher.getChannelName().equals(newPub.getChannelName())) {
                                registeredPublishers.remove(publisher);
                                registeredPublishers.add(newPub);
                            }
                        }
                        for(PublisherImpl publisher: registeredPublishersBasedOnHashTag){
                            if(publisher.getChannelName().equals(newPub.getChannelName())) {
                                registeredPublishersBasedOnHashTag.remove(publisher);
                                registeredPublishersBasedOnHashTag.add(newPub);
                            }
                        }
                        brokerHashtags = null;
                        for(PublisherImpl publisher: registeredPublishersBasedOnHashTag){
                            for(String hashtag: publisher.getMyHashTags()){
                                if(!brokerHashtags.contains(hashtag)){
                                    brokerHashtags.add(hashtag);
                                }
                            }
                        }

                    }else{

                        double newPubHash = getMd5(newPub.getChannelName());

                        if(newPubHash < getBrokerHashNumber() && newPubHash >= getLowerBrokerHashNumber()) {
                            registeredPublishers.add(newPub);
                            System.out.println("registeredPublishers.size() = " + registeredPublishers.size());
                        }
                        boolean flag = false;

                        double hashtagNumber;
                        for(String hashtag: newPub.getMyHashTags()){
                            hashtagNumber = getMd5(hashtag);
                            if(hashtagNumber < getBrokerHashNumber() && hashtagNumber >= getLowerBrokerHashNumber()){
                                flag = true;
                                if(!brokerHashtags.contains(hashtag)){
                                    brokerHashtags.add(hashtag);
                                }
                            }
                        }
                        if(flag=true){
                            registeredPublishersBasedOnHashTag.add(newPub);
                        }

                    }

                    message = new Message(getRegisteredPublishers());
                    out.writeObject(message);
                    message = new Message(getBrokerHashTags(),1);
                    out.writeObject(message);

                    closeConnection();

                    System.out.println(getBrokerHashTags().toString());

                }

            } // end while
			
		} catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
	}

    private void notifyBrokersOnChanges(PublisherImpl newPub,String typeOfAction) {

        Socket informSocket;
        ObjectInputStream in;
        ObjectOutputStream out;
        Message message;

        try {
            for (int i = 1; i < 3; i++) {

                informSocket = new Socket(activeBrokers[i].getIP(), activeBrokers[i].getPort());
                out = new ObjectOutputStream(informSocket.getOutputStream());
                in = new ObjectInputStream(informSocket.getInputStream());
                out.writeObject(new Message(type));
                out.flush();
                out.writeObject(new Message(typeOfAction));
                out.flush();
                out.writeObject(new Message(newPub.getPublisherIP(), newPub.getPublisherPort(),newPub.getMyHashTags(),newPub.getMyVideoFiles(),newPub.getChannelName(),newPub.getVideosConnectedWithHashtags()));
                out.flush();

                message = (Message)in.readObject();
                activeBrokers[i].setRegisteredPublishers(message.getPublishersList());
                message = (Message)in.readObject();
                activeBrokers[i].setBrokerHashtags(message.getBrokerHashtags());

                informSocket.close();
                in.close();
                out.close();
            }
            System.out.println("Information sent ,about new Publisher, to other Brokers");

        }catch (IOException | ClassNotFoundException e) {
            System.err.println("Problem on connection with other brokers");
        }
    }


	private void closeConnection()
    {
        displayMessage( "\nTerminating connection\n" );

        try
        {
            out.close();
            in.close();
            connection.close(); // close socket
        } // end try
        catch ( IOException ioException )
        {
            ioException.printStackTrace();
        } // end catch
    }

    private void acceptConnectionForConsumer(){

	    Thread thread = new ActionsForConsumers(connection,in,out,getIP(),getPort(),getId(),getRegisteredPublishers(),this.activeBrokers,getBrokerHashTags(),getRegisteredPublishersBasedOnHashtag());

        thread.start();

    }


    private void displayMessage( String messageForDisplay ) {
		 
		 System.out.println(messageForDisplay+"\n");
		 
	 }

    public List<Message> pullVideo(int publisherPort, String publisherIP,String request) {
        try {
            Socket initConnect;
            initConnect = new Socket(publisherIP, publisherPort);

            ObjectInputStream dis = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());

            dos.writeObject(new Message(request));
            System.out.println(((Message)dis.readObject()).getString());

            List<Message> array = new ArrayList<>();
            Message toreturn = (Message) dis.readObject();
            array.add(0, toreturn);
            for(int i = 1; i<toreturn.getNumber(); i++){
                array.add(i, (Message) dis.readObject());
            }
            initConnect.close();
            dis.close();
            dos.close();
            return array;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

       String typeOfcase = args[0];
       BrokerImpl[] brokers = new BrokerImpl[10];

       if(typeOfcase.equals("1")) {

           brokers[0] = new BrokerImpl(getSystemIP(), 6000, 1);
           brokers[1] = new BrokerImpl(getSystemIP(), 7000, 2);
           brokers[2] = new BrokerImpl(getSystemIP(), 8000, 3);

           for(int i=0;i<3;i++){
               brokers[0].activeBrokers[i]=brokers[i];
           }

           brokers[0].init();
       }else if(typeOfcase.equals("2")){

           brokers[0] = new BrokerImpl(getSystemIP(), 7000, 2);
           brokers[1] = new BrokerImpl(getSystemIP(), 6000, 1);
           brokers[2] = new BrokerImpl(getSystemIP(), 8000, 3);

           for(int i=0;i<3;i++){
               brokers[0].activeBrokers[i]=brokers[i];
           }

           brokers[0].init();

       }else{

           brokers[0] = new BrokerImpl(getSystemIP(), 8000, 3);
           brokers[1] = new BrokerImpl(getSystemIP(), 7000, 2);
           brokers[2] = new BrokerImpl(getSystemIP(), 6000, 1);

           for(int i=0;i<3;i++){
               brokers[0].activeBrokers[i]=brokers[i];
           }

           brokers[0].init();
       }

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

    private class ConsoleColors {
        // Reset
        public static final String RESET = "\033[0m";  // Text Reset

        // Regular Colors
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
    }

    public static double getMd5(String input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            String hashtext = no.toString(16);
            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }
            BigInteger big;
            big = new BigInteger(hashtext, 16);
            double md5 = big.doubleValue();
            return md5%100;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
