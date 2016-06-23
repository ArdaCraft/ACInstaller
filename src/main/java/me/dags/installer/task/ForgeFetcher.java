package me.dags.installer.task;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dags <dags@dags.me>
 */
public class ForgeFetcher {

    private static final String LOOK_UP = "promos";
    private static final String VERSION_LIST = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
    private static final String INSTALLER_URL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-installer.jar";

    private final String mc_version;
    private final String release_lookup;

    private String url = "";
    private Path path = Paths.get("");

    public ForgeFetcher(String mc_version, String release_stream) {
        this.mc_version = mc_version;
        this.release_lookup = mc_version + "-" + release_stream;
        System.out.println(release_lookup);
    }

    public String url() {
        return url;
    }

    public Path path() {
        return path;
    }

    public void run() {
        try {
            URL url = new URL(VERSION_LIST);
            JsonElement element = new JsonParser().parse(new InputStreamReader(url.openConnection().getInputStream()));
            JsonObject promos = element.getAsJsonObject().getAsJsonObject(LOOK_UP);

            String recommended = promos.get(release_lookup).getAsString();
            String download = String.format(INSTALLER_URL, mc_version, recommended);
            String fileName = download.substring(download.lastIndexOf('/') + 1);

            Path out = new File(new File("").getAbsolutePath(), fileName).toPath();

            this.url = download;
            this.path = out;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launch(Path path) {
        try {
            Process process = Runtime.getRuntime().exec("java -jar " + path);
            while (process.isAlive()) {}
            System.out.println("Process closed!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
