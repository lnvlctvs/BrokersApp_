import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import static java.lang.System.exit;


public class ConsumerImpl {
    private static Socket requestSocket = null;
    private static ObjectOutputStream consumerOut = null;
    private static ObjectInputStream consumerIn = null;


    private static double hashBroker1;
    private static double hashBroker2;
    private static double hashBroker3;

    public static List<BrokerImpl> brokers = new ArrayList<>();
    private static final String type = "consumer";

    public ConsumerImpl() {}

    public static void initConsumer() {
        try {
            Message typeOfUser;
            Message flag;
            Message request;
            Message success;
            requestSocket = new Socket(getSystemIP(), 6000);
            consumerOut = new ObjectOutputStream(requestSocket.getOutputStream());
            consumerIn = new ObjectInputStream(requestSocket.getInputStream());

            System.out.println("Connection with broker initialized\nBroker : " + requestSocket);

            typeOfUser = new Message(type);
            consumerOut.writeObject(typeOfUser);
            consumerOut.flush();

            // Saying to the broker Thread that this is the first time I'm connecting.
            flag = new Message("firsttime");
            consumerOut.writeObject(flag);
            consumerOut.flush();


            request = (Message)consumerIn.readObject();
            int j =(request.getNumber());


            for(int i=0; i < j-2; i++){

                request = (Message)consumerIn.readObject();
                brokers.add(new BrokerImpl(request.getIP(),request.getPort(),request.getID(),request.getPublishersList(),request.getBrokerHashtags()));
                success = new Message("Broker: " +brokers.get(i).id + " received");
                consumerOut.writeObject(success);

            }

            hashBroker1 = ((Message)consumerIn.readObject()).getMd5HashNumber();
            hashBroker2 = ((Message)consumerIn.readObject()).getMd5HashNumber();
            hashBroker3 = ((Message)consumerIn.readObject()).getMd5HashNumber();

            System.out.println(hashBroker1);
            System.out.println(hashBroker2);
            System.out.println(hashBroker3);

            System.out.println("Consumer connected to broker");
            disconnect(requestSocket,consumerIn, consumerOut);

            register();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void disconnect(Socket requestSocket,ObjectInputStream in,ObjectOutputStream out) {
        try {
            requestSocket.close();
            in.close();
            out.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    public static void register() {
        //PRINTS ALL AVAILABLE CHANNELNAMES AND HASHTAGS OF EVERY BROKER
        System.out.print("The available ChannelNames and Hashtags in each Broker are the following : \n");
        for(int i=0; i<brokers.size();i++){
            System.out.print(ConsoleColors.RED+"Broker: "+brokers.get(i).getId()+ "\n"+ConsoleColors.RESET);
            System.out.print("Registered Publishers : ");
            for(PublisherImpl publisher:brokers.get(i).getRegisteredPublishers()){
                System.out.print(ConsoleColors.CYAN+publisher.getChannelName()+" "+ConsoleColors.RESET);
            }
            System.out.println();
            System.out.print("Available Hashtags : ");
            System.out.println(" "+ConsoleColors.GREEN+brokers.get(i).getBrokerHashTags().toString()+" "+ConsoleColors.RESET);
            System.out.println();
        }

        Scanner scn = new Scanner(System.in);
        System.out.print("Enter ChannelName / Hashtag to see Videos of your choice or type Exit: ");
        System.out.print("");
        String requestToBroker = scn.nextLine();
        if(requestToBroker.equalsIgnoreCase("exit")){
            System.out.println("Consumer exiting ... \nGoodbye!");
            exit(0);
        }

        //now we have to connect with the correct broker based on the hashing of the String we requested

        if (getMd5(requestToBroker) < hashBroker1)
            connectToMakeRequest(brokers.get(0), requestToBroker);
        else if(getMd5(requestToBroker) < hashBroker2)
            connectToMakeRequest(brokers.get(1), requestToBroker);
        else
            connectToMakeRequest(brokers.get(2), requestToBroker);

    }

    /**Performs all the actions required to provide the user the VideoFile he wants*/
    public static void connectToMakeRequest(BrokerImpl b, String requestToBroker) {

        Message answerFromBroker;

        try {
            requestSocket = new Socket(b.getIP(), b.port);
            consumerOut = new ObjectOutputStream(requestSocket.getOutputStream());
            consumerIn = new ObjectInputStream(requestSocket.getInputStream());

            Message message = new Message(type);
            consumerOut.writeObject(message);
            consumerOut.flush();

            message = new Message("request");
            consumerOut.writeObject(message);
            consumerOut.flush();

            // HERE WE MAKE A REQUEST TO THE BROKER THREAD FOR THE VIDEO WE WANT TO SEE BASED ON HASHTAG OR CHANNELNAME
            message = new Message(requestToBroker);
            consumerOut.writeObject(message);
            consumerOut.flush();

            answerFromBroker = (Message)consumerIn.readObject();
            String reply = answerFromBroker.getString();

            if(reply.equalsIgnoreCase("error")){
                System.err.println("Something went wrong.\nThe request you've made wasn't found");
                System.err.println("Either your request doesn't exist OR you typed it wrong");
            } else {

                Scanner scn = new Scanner(System.in);

                List<String> availableVideos;
                answerFromBroker = (Message) consumerIn.readObject();
                availableVideos = answerFromBroker.getListOfVideos();
                System.out.println("___Choose a video from the following List below___");
                System.out.println("\n" + availableVideos.toString() + "\n");

                String videoRequest;

                for (; ; ) {
                    System.out.print("Enter the VideoName you want to see here ---> ");
                    videoRequest = scn.nextLine();

                    if (availableVideos.contains(videoRequest)) {
                        break;
                    } else {
                        System.out.println("Error, the video you typed wasn't found or you typed it wrong! TRY AGAIN!!!");
                    }
                }

                message = new Message(videoRequest);
                consumerOut.writeObject(message);
                consumerOut.flush();

                int size = ((Message) consumerIn.readObject()).getNumber();

                byte[] newVideoFileExtract;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                for (int i = 0; i < size; i++) {
                    Message info = ((Message) consumerIn.readObject());
                    outputStream.write(info.getVideoChunk());
                }

                newVideoFileExtract = outputStream.toByteArray();
                try (FileOutputStream stream = new FileOutputStream("ConsumerVideos\\" + videoRequest + "_.mp4")) {
                    stream.write(newVideoFileExtract);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            disconnect(requestSocket,consumerIn, consumerOut);

            // Return to register() method to make new REQUEST
            register();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) {
        initConsumer();
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

    private static class ConsoleColors {
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
}
