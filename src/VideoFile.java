import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class VideoFile implements Serializable {

    private String videoName;
    private String channelName;
    private String dateCreated;
    private String length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private ArrayList<String> associatedHashtags = new ArrayList<>();
    private byte[] videoFileChunk;
    private byte[] videoFileExtract;
    private byte[][] chunkArray;

    /**Copy Constructor*/
    public VideoFile(String videoName,int i,int numType){

        File folder;
        if(numType==1)
            folder = new File("PUB1 VIDEOS");
        else
            folder = new File("PUB2 VIDEOS");


        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if(!file.getName().startsWith(".")) {
                try {
                    VideoFile mp4 = new VideoFile(file);
                    if(mp4.getVideoName()!=null) {
                        if (mp4.getVideoName().equalsIgnoreCase(videoName)) {
                            this.videoFileExtract = Files.readAllBytes(file.toPath());
                            this.chunkArray = divideArray(videoFileExtract, 512000);
                            this.videoFileChunk = chunkArray[i];
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error in chunks");
                } catch (TikaException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public VideoFile(String videoName,ArrayList<String> associatedHashtags){
        this.videoName = videoName;
        this.associatedHashtags = associatedHashtags;
    }


    /**Constructs a new video file*/

    public VideoFile(File videoFile) throws IOException,SAXException, TikaException {

        //detecting the file type
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(videoFile);
        ParseContext pcontext = new ParseContext();

        MP4Parser MP4Parser = new MP4Parser();
        MP4Parser.parse(inputstream, handler, metadata,pcontext);
        String[] metadataNames = metadata.names();

        // Set metadata values
        String[] title = splitTitle(metadata.get("title"));
        this.videoName = title[0];

        for (int i=1; i < title.length; i++) {
            associatedHashtags.add(title[i]);
        }

        this.dateCreated = metadata.get("Creation-Date");
        this.frameHeight = metadata.get("tiff:ImageLength");
        this.frameWidth = metadata.get("tiff:ImageWidth");
        this.length = metadata.get("xmpDM:duration");

    }


    public String[] splitTitle(String title) {
        String[] output = title.split("#");
        return output;
    }

    public static byte[][] divideArray(byte[] source, int chunksize) {


        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }

    /** get methods **/
    public byte[] getVideoFileExtract(){
        return videoFileExtract;
    }

    public byte[] getVideo(int i) {
        return chunkArray[i];
    }

    public String getVideoName(){
        return videoName;
    }

    public String getChannelName(){
        return channelName;
    }

    public String getDateCreated(){
        return dateCreated;
    }

    public String getLength(){
        return length;
    }

    public String getFramerate(){
        return framerate;
    }

    public String getFrameWidth(){
        return frameWidth;
    }

    public String getFrameHeight(){
        return frameHeight;
    }

    public ArrayList <String> getAssociatedHashtags(){
        return associatedHashtags;
    }

    public byte[] getVideoFileChunk(){
        return videoFileChunk;
    }

    public byte[][] getChunkArray() {
        return chunkArray;
    }

    public byte[] getMusicFileExtract() {
        return videoFileExtract;
    }

    /** To String method **/

    @Override
    public String toString(){
        return  "\nVideo Title: " + videoName +
                "\nFrame Width: " + frameWidth +
                "\nFrame Height: " + frameHeight +
                "\nDate Created: " + dateCreated +
                "\nLength: " + length;
    }
}
