package advisor;

public class Artist {
    private String name;

    public Artist()
    {
        this("Unspecified");
    }
    public Artist(String name)
    {
        this.name = name;
    }
    @Override
    public String toString()
    {
        return name;
    }
}
