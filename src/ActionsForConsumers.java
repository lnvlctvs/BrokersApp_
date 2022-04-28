import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ActionsForConsumers extends Thread{
	
	private final ObjectInputStream input;
	private final ObjectOutputStream output;
	private final Socket connection;
	private ArrayList<PublisherImpl> wantedPublishers;
	private List<String> videos;
	private final BrokerImpl currentBroker;
	private final BrokerImpl[] listOfBrokers;
	
	public ActionsForConsumers(Socket connection, ObjectInputStream in, ObjectOutputStream out,String ip, int port, int id, ArrayList<PublisherImpl> registeredPublishers,BrokerImpl[] listOfBrokers,ArrayList<String> brokerhashtags,ArrayList<PublisherImpl> registered_pubs_based_on_hashtag) {

		// Takes Socket and Streams of the client ,for reading and writing from the BrokerImpl.
		this.connection=connection;
		this.input=in;
		this.output=out;
		currentBroker = new BrokerImpl(ip,port,id,registeredPublishers,brokerhashtags,registered_pubs_based_on_hashtag);
		this.listOfBrokers = listOfBrokers;

	}
	
	public void run() {

		Message message;
		Message flag;
		Message requestOfConsumer;

		// "if" runs for consumer's first connection to broker System
		// "else" runs when the consumer is ready to request VideoFiles
 		try {

			flag = (Message)input.readObject();

			if(flag.getString().equals("firsttime")) {

				message = new Message(listOfBrokers.length);
				output.writeObject(message);
				output.flush();

				// Broker Thread sends ,at first connection ,list of Brokers to Consumer.
				for (int i = 0; i < listOfBrokers.length - 2; i++) {

					message = new Message(listOfBrokers[i]);
					output.writeObject(message);
					System.out.println(((Message) input.readObject()).getString());

				}

				output.writeObject(new Message(listOfBrokers[0].getBrokerHashNumber()));
				output.writeObject(new Message(listOfBrokers[1].getBrokerHashNumber()));
				output.writeObject(new Message(listOfBrokers[2].getBrokerHashNumber()));

			}else {

				requestOfConsumer = (Message) input.readObject();
				String request = requestOfConsumer.getString();

				boolean found = false;
				PublisherImpl requestedPublisher = null;

				for (PublisherImpl publisher : currentBroker.getRegisteredPublishers()) {
					if (publisher.getChannelName().equalsIgnoreCase(request)) {
						requestedPublisher = publisher;
						found = true;
						break;
					}
				}

				if (!found) {
					wantedPublishers = new ArrayList<>();
					videos = new ArrayList<>();
					for (PublisherImpl publisher : currentBroker.getRegisteredPublishersBasedOnHashtag()) {
						for (VideoFile video : publisher.getVideosConnectedWithHashtags()) {
							for (String hashtag : video.getAssociatedHashtags()) {
								if (hashtag.equalsIgnoreCase(request)) {
									wantedPublishers.add(publisher);
									videos.add(video.getVideoName());
									found = true;
								}
							}
						}
					}
				}

				if(found){

					if (requestedPublisher != null) {

						output.writeObject(new Message("ChannelVideosIncoming"));
						output.flush();

						output.writeObject(new Message(requestedPublisher.getMyVideoFiles()));
						output.flush();

						requestOfConsumer = (Message) input.readObject();
						request = requestOfConsumer.getString();


						List<Message> channelVideo;
						channelVideo = currentBroker.pullVideo(requestedPublisher.getPublisherPort(), requestedPublisher.getPublisherIP(), request);
						output.writeObject(new Message(channelVideo.size()));

						for (int i = 0; i < channelVideo.size(); i++) {
							output.writeObject(channelVideo.get(i));
						}

					} else {
						output.writeObject(new Message("HashtagVideosIncoming"));
						output.flush();

						output.writeObject(new Message(videos));
						output.flush();

						requestOfConsumer = (Message) input.readObject();
						request = requestOfConsumer.getString();

						for(PublisherImpl publisher: wantedPublishers){
							if(publisher.getMyVideoFiles().contains(request))
								requestedPublisher = publisher;
						}

						List<Message> channelVideo;
						channelVideo = currentBroker.pullVideo(requestedPublisher.getPublisherPort(), requestedPublisher.getPublisherIP(), request);
						output.writeObject(new Message(channelVideo.size()));

						for (int i = 0; i < channelVideo.size(); i++) {
							output.writeObject(channelVideo.get(i));
						}

					}
				}else{
						output.writeObject(new Message("error"));
						output.flush();
					}

			}
			connection.close();
			output.close();
			input.close();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
