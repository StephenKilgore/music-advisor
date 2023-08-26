package advisor;

public class Playlist {
    private String title;
    private String link;

    public Playlist()
    {
        this("Undefined", "Undefined");
    }
    public Playlist(String title)
    {
        this(title, "Undefined");
    }
    public Playlist(String title, String link)
    {
        this.title = title;
        this.link = link;
    }
    @Override
    public String toString()
    {
        return title + "\n" + link + "\n";
    }
}
