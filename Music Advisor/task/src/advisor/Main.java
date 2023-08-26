package advisor;

public class Main {
    public static void main(String[] args) {
        MusicAdvisor advisor = new MusicAdvisor();
        String accessPoint = args.length == 6 && "-access".equals(args[0])  ? args[1] : "https://accounts.spotify.com";
        String resources = args.length == 6 && "-resource".equals(args[2]) ? args[3] : "https://api.spotify.com";
        int page = args.length == 6 && "-page".equals(args[4]) ? Integer.parseInt(args[5]) : 1;
        advisor.start(accessPoint, resources, page);
    }
}
