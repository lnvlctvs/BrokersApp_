import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<String> brokerHashtags;
	private List<String> videoNames;

	private List<VideoFile> videosConnectedWithHashtags;
	private String msg;
	private int port;
	private int id;
	private String ip;
	private int number;
	private double md5HashNumber;
	private String channelName;
	private byte[] videoChunk;
	private List<String> myHashTags;
	private ArrayList<PublisherImpl> publishersList;

	public Message(String msg) {

		this.msg = msg;
	}

	public Message(int msg) {

		this.number = msg;
	}

	public Message(double md5hash){

		this.md5HashNumber = md5hash;
	}

	public Message(String ip,int port, List<String> myHashTags,List<String> videoNames,String name,List<VideoFile> videosConnectedWithHashtags) {
		this.ip = ip;
		this.port = port;
		this.myHashTags = myHashTags;
		this.videoNames= videoNames;
		this.channelName = name;
		this.videosConnectedWithHashtags = videosConnectedWithHashtags;
	}

	public Message(ArrayList<String> hashtags,int n){

		this.brokerHashtags = hashtags;
	}

	public Message(List<String> listOfStrings){

		this.videoNames = listOfStrings;
	}

	public Message(BrokerImpl broker) {
		this.ip = broker.getIP();
		this.port = broker.getPort();
		this.id = broker.getId();
		this.publishersList  = broker.getRegisteredPublishers();
		this.brokerHashtags = broker.getBrokerHashTags();

	}

	public Message(ArrayList<PublisherImpl> pub) {

		this.publishersList = pub;
	}

	public Message(ChannelName n) {
		this.channelName = n.getChannelName();
		this.videoNames = n.getVideoNames();
	}

	public Message(byte[] videoChunk,int n) {
		this.videoChunk = videoChunk;
		this.number = n;
	}

	public byte[] getVideoChunk() {

		return videoChunk;
	}

	public List<String> getListOfVideos(){

		return videoNames;
	}

	public double getMd5HashNumber(){

		return md5HashNumber;
	}

	public String getIP(){

		return ip;
	}

	public String getString() {

		return msg;
	}

	public String getChannelName(){

		return channelName;
	}

	public int getPort() {

		return port;
	}

	public int getID() {

		return id;
	}

	public int getNumber(){

		return number;
	}

	public List<String> getListOfHashTags() {

		return myHashTags;
	}

	public List<String> getVideoNames() {

		return videoNames;
	}

	public ArrayList<PublisherImpl> getPublishersList() {

		return publishersList;
	}

	public ArrayList<String> getBrokerHashtags(){

		return brokerHashtags;
	}

	public List<VideoFile> getVideosConnectedWithHashtags(){
		return videosConnectedWithHashtags;
	}
	
	
}
