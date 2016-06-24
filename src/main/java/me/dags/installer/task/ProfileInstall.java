package me.dags.installer.task;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.installer.Installer;

import java.io.*;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
public class ProfileInstall implements Runnable {

    private final String forgeProfile;
    private final String installDir;

    public ProfileInstall(String profile, Path installPath) {
        this.forgeProfile = profile;
        this.installDir = installPath.toString();
    }

    @Override
    public void run() {
        try {
            Installer.phase("profile get").log("Adding launcher profile for {}...", Installer.properties().profile_name);
            File profilesFile = new File(Installer.properties().mcDir, "launcher_profiles.json");
            if (!profilesFile.exists())
            {
                Installer.phase("error").log("File does not exist: {}", profilesFile);
                return;
            }
            JsonObject profile = new JsonObject();
            profile.addProperty("name", Installer.properties().profile_name);
            profile.addProperty("gameDir", installDir);
            profile.addProperty("lastVersionId", forgeProfile);
            Installer.phase("profile edit").log("Appending profile {}", profile.toString());

            FileInputStream in = new FileInputStream(profilesFile);
            JsonObject profiles = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
            profiles.get("profiles").getAsJsonObject().add(Installer.properties().profile_name, profile);
            in.close();

            Installer.phase("profile write").log("Writing profiles to disk...");
            FileWriter writer = new FileWriter(profilesFile);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(profiles));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
