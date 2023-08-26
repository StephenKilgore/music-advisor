package advisor;

import java.util.Arrays;
import java.util.LinkedList;

public class Album {
    private String title;
    private LinkedList<Artist> artists = new LinkedList<Artist>();
    private LinkedList<Song> trackList;
    private String link;

    public Album()
    {
        title = "Undefined";
    }
    public Album(String title, String link, Artist...artists)
    {
        this.title = title;
        this.artists.addAll(Arrays.asList(artists));
        this.link = link;
    }
    @Override
    public String toString() {

        return title + "\n" + artists + "\n" + link +"\n";
    }
}
