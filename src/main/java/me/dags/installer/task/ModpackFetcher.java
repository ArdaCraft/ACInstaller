package me.dags.installer.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author dags <dags@dags.me>
 */
public class ModpackFetcher {

    private static final String tags_list = "https://api.github.com/repos/%1$s/%2$s/tags";

    private final String user;
    private final String repo;

    private String tag = "";
    private String url = "";
    private String fileName = "";

    public ModpackFetcher(String user, String repo) {
        this.user = user;
        this.repo = repo;
    }

    public void run() {
        try {
            URL url = new URL(String.format(tags_list, user, repo));
            JsonElement element = new JsonParser().parse(new InputStreamReader(url.openConnection().getInputStream()));
            JsonArray tags = element.getAsJsonArray();
            JsonObject first = tags.get(0).getAsJsonObject();

            String name = first.get("name").getAsString();
            String download = first.get("zipball_url").getAsString();
            String fileName = download.substring(download.lastIndexOf("/") + 1) + ".zip";

            this.url = download;
            this.tag = name;
            this.fileName = fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String url() {
        return url;
    }

    public String tag() {
        return tag;
    }

    public String fileName() {
        return fileName;
    }
}
