package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class MusicAdvisor {
    private LinkedList<Album> newReleases = new LinkedList<Album>();
    private LinkedList<Playlist> featuredPlaylists = new LinkedList<Playlist>();
    private LinkedList<Category> allCategories = new LinkedList<Category>();
    private HashMap<String,Category>  categoryMap = new HashMap<>();
    private boolean isAuthed;
    private String authCode = "";
    private String access_code = "";
    private boolean serverRunning = false;
    private String accessPoint = "";
    private String resources = "";
    private ViewHandler vh;
    private int pageSize;
    private String promptUser() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    private String handleChoice(String choice) {
        String[] args = choice.split(" ", 2);
        if (!isAuthed && !"auth".equals(args[0]) && !"exit".equals(args[0])
                && !"help".equals(args[0]))
        {
            System.out.println("Please, provide access for application.");
            return "";
        }
        switch (args[0]) {
            case "exit":
//                handleExit();
//                return "exit";
                break;
            case "new":
                handleNewReleases();
                break;
            case "featured":
                handleFeatured();
                break;
            case "categories":
                handleCategories();
                break;
            case "auth":
                handleAuth();
                break;
            case "playlists":
                if (args.length > 1) {
                    handlePlaylists(args[1]);
                }
                break;
            case "help":
                handleHelp();
                break;
            case "next":
                if (vh !=null) {
                    vh.next();
                    break;
                }
                break;
            case "prev":
                if ( vh != null) {
                    vh.prev();
                    break;
                }
                break;
            default:
                System.out.println("Unknown option: " + args[0] + ". Use 'help' for more info.");
                break;
        }
        return "";
    }
    private void handleExit()
    {
        System.out.println("---GOODBYE!---");
    }
    private void handleNewReleases()
    {
        getNewReleases();
        showNewReleases();
    }
    private void getNewReleases()
    {
        try {
            newReleases.clear();
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + access_code)
                    .uri(URI.create(resources + "/v1/browse/new-releases"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            JsonObject responseObj = JsonParser.parseString(response.body()).getAsJsonObject();
            for (JsonElement ele : responseObj.get("albums").getAsJsonObject().get("items").getAsJsonArray())
            {
                ArrayList<Artist> artists = new ArrayList<>();
                for (JsonElement artist : ele.getAsJsonObject().get("artists").getAsJsonArray())
                {
                    artists.add(new Artist(artist.getAsJsonObject().get("name").getAsString()));
                }
                Artist[] artistArr = new Artist[artists.size()];
                newReleases.add(new Album(ele.getAsJsonObject().get("name").getAsString(),ele
                        .getAsJsonObject()
                        .get("external_urls").getAsJsonObject().get("spotify").getAsString(), artists.toArray(artistArr)));
            }

        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void showNewReleases()
    {
        vh = new ViewHandler<Album>(pageSize);
        vh.setObjList(newReleases);
        vh.display();
    }
    private void handleFeatured()
    {
        getFeaturedPlaylists();
        showFeaturedPlaylists();
    }
    private void getFeaturedPlaylists()
    {
        featuredPlaylists.clear();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + access_code)
                    .uri(URI.create(resources + "/v1/browse/featured-playlists"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            JsonObject responseObj = JsonParser.parseString(response.body()).getAsJsonObject();
            for (JsonElement ele : responseObj.get("playlists").getAsJsonObject().get("items").getAsJsonArray())
            {
                featuredPlaylists.add(new Playlist(ele.getAsJsonObject().get("name").getAsString(),ele
                        .getAsJsonObject()
                        .get("external_urls").getAsJsonObject().get("spotify").getAsString()));
            }
        } catch (Exception e)
        {

        }
    }
    private void showFeaturedPlaylists()
    {
        vh = new ViewHandler<Playlist>(pageSize);
        vh.setObjList(featuredPlaylists);
        vh.display();
    }
    private Playlist createPlaylist(String title)
    {
         return createPlaylist(title, "Undefined");
    }
    private Playlist createPlaylist(String title, String link)
    {
        return new Playlist(title, link);
    }
    private void handleCategories()
    {
        getCategories();
        showCategories();
    }
    private void getCategories()
    {
        allCategories.clear();
        categoryMap.clear();
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + access_code)
                    .uri(URI.create(resources + "/v1/browse/categories"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            JsonObject responseObj = JsonParser.parseString(response.body()).getAsJsonObject();
            for (JsonElement ele : responseObj.get("categories").getAsJsonObject().get("items").getAsJsonArray())
            {
                allCategories.add(createCategory(ele.getAsJsonObject().get("name").getAsString(),
                        ele.getAsJsonObject().get("id").getAsString()));
            }
        } catch (Exception e)
        {

        }
    }
    private void showCategories()
    {
        vh = new ViewHandler<Category>(pageSize);
        vh.setObjList(allCategories);
        vh.display();
    }
    private Category createCategory(String name, String id)
    {
        Category c = new Category();
        if (!categoryMap.containsKey(name))
        {
            c= new Category(name, id);
            categoryMap.put(name, c);
        }
        return c;
    }
    private void handlePlaylists(String categoryName)
    {
        showPlaylistsByCategory(categoryName, getPlaylistsByCategory(categoryName));
    }
    private LinkedList<Playlist> getPlaylistsByCategory(String categoryName)
    {
        allCategories.clear();
        try {
            LinkedList<Playlist> playlists = new LinkedList<>();
            Category c = categoryMap.get(categoryName);
            if (c == null)
            {
                System.out.println("Specified id doesn't exist");
                return null;
            }
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + access_code)
                    .uri(URI.create(resources + "/v1/browse/categories/" + c.getId() + "/playlists"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject responseObj = JsonParser.parseString(response.body()).getAsJsonObject();
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                for (JsonElement ele : responseObj.get("playlists").getAsJsonObject().get("items").getAsJsonArray()) {
                    playlists.add(new Playlist(ele.getAsJsonObject().get("name").getAsString(), ele
                            .getAsJsonObject()
                            .get("external_urls").getAsJsonObject().get("spotify").getAsString()));
                }
                return playlists;
            }
            else {
                System.out.println(responseObj.getAsJsonObject().get("message").getAsString());
            }
            return null;
        }
        catch (IOException e)
        {
            System.out.print("Specified id doesn't exist");
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    private void showPlaylistsByCategory(String categoryName, LinkedList<Playlist> playlists)
    {
        vh = new ViewHandler<Playlist>(pageSize);
        vh.setObjList(playlists);
        vh.display();
    }
    public void handleAuth()
    {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            try {
                                if (!isAuthed) {

                                    String query = exchange.getRequestURI().getQuery();
                                    String response;
                                    if (query != null && query.contains("code")) {
                                        authCode = query.substring(5);
                                        System.out.println("code received");
                                        response = "Got the code. Return back to your program.";
                                        exchange.sendResponseHeaders(200, response.length());
                                        exchange.getResponseBody().write(response.getBytes());
                                        exchange.getResponseBody().close();
                                    } else {
                                        response = "Authorization code not found. Try again.";
                                        exchange.sendResponseHeaders(200, response.length());
                                        exchange.getResponseBody().write(response.getBytes());
                                        exchange.getResponseBody().close();
                                        return;
                                    }
                                    System.out.println("code received");
                                    System.out.println("making http request for access_token...");

                                    HttpClient httpClient = HttpClient.newHttpClient();

                                    String authString = "c0eae5105be0467ab48ca6db7e20da4f:fa004c9b92374701b39143b300fcbb08";

                                    HttpRequest request = HttpRequest.newBuilder()
                                            .header("Content-Type", "application/x-www-form-urlencoded")
                                            .header("Authorization", "Basic" + " " + Base64.getEncoder().encodeToString(authString.getBytes()))
                                            .uri(URI.create(accessPoint + "/api/token"))
                                            .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&code=" + authCode
                                                    + "&redirect_uri=http://localhost:8080"))
                                            .build();

                                    HttpResponse<String> authResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                                    if (authResponse.statusCode() == 200) {
                                        JsonObject responseObj = JsonParser.parseString(authResponse.body()).getAsJsonObject();
                                        access_code = responseObj.get("access_token").getAsString();
                                        System.out.println("Success!");

                                        isAuthed = true;
                                        getCategories();
                                    }
                                    serverRunning = false;
                                    server.stop(0);
                                }
                            } catch (Exception e)
                            {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
            );
            server.start();
            serverRunning = true;

            System.out.println("use this link to request the access code:");
            System.out.println(accessPoint + "/authorize?client_id=c0eae5105be0467ab48ca6db7e20da4f&redirect_uri=http://localhost:8080&response_type=code");
            System.out.println("waiting for code...");
            while (serverRunning)
            {
                Thread.sleep(5000);
            }
        }
        catch (Exception e)
        {
            System.out.print(e.getMessage());
        }
    }
    public void handleHelp()
    {
        System.out.println("---HELP---");
        System.out.println("The following options are available:");
        System.out.println();
        System.out.println("auth: authorize the application.");
        System.out.println("new: show new albums.");
        System.out.println("auth: authorize the application.");
        System.out.println("featured: see the featured playlists.");
        System.out.println("categories: see a list of all categories in Spotify.");
        System.out.println("playlists [category name]: show a list of playlists by category names.");
        System.out.println("help: show the help list");
    }
    public void start(String accessPoint, String resources, int pageSize)
    {
        this.accessPoint = accessPoint;
        this.resources = resources;
        this.pageSize = pageSize;

        while (true)
        {
            String choice = handleChoice(promptUser());
            if ("exit".equals(choice))
            {
                break;
            }
        }
    }
}
