import java.util.List;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class ChannelName {

    private final List<String> hashtagsPublished;
    private final String ChannelName;
    private final List <String> videoNames;
    private final List<VideoFile> videosConnectedWithHashtags;
    public int type;

    public ChannelName(String name,int p) throws TikaException, SAXException, IOException {
        this.type = p;
        this.ChannelName = name;
        this.hashtagsPublished = new ArrayList<>();
        this.videosConnectedWithHashtags = new ArrayList<>();
        this.videoNames = new ArrayList<>();
        // IF works so that, different publisher's can access the same folder with mp4 videos and get different files, in case the program runs locally.
        File file;
        if(p==1)
            file = new File("PUB1 VIDEOS"); // path of the channel videos
        else
            file = new File("PUB2 VIDEOS"); // path of the channel videos
        File[] files = file.listFiles();
        VideoFile mp4;
        assert files != null;
        for (File f:files) {
                mp4 = new VideoFile(f);
                videoNames.add(mp4.getVideoName());
                videosConnectedWithHashtags.add(new VideoFile(mp4.getVideoName(), mp4.getAssociatedHashtags()));

                for (String hashtag : mp4.getAssociatedHashtags()) {
                    if (!hashtagsPublished.contains(hashtag))
                        hashtagsPublished.add(hashtag);
                }

        }
    }

    public String getChannelName() {

        return ChannelName;
    }
    public List <String> getVideoNames() {

        return videoNames;
    }

    public List<String> getHashTagsPublished() {

        return hashtagsPublished;
    }

    public List<VideoFile> getVideosConnectedWithHashtags(){
        return videosConnectedWithHashtags;
    }
}
