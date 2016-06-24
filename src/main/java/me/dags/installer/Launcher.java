package me.dags.installer;

import com.google.gson.Gson;
import me.dags.installer.ui.ImageLayer;
import me.dags.installer.ui.InstallerPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * @author dags <dags@dags.me>
 */
public class Launcher {

    public static void main(String[] args) {

        InputStream inputStream = Launcher.class.getResourceAsStream("/properties.json");
        Properties properties = new Gson().fromJson(new InputStreamReader(inputStream), Properties.class);
        Installer.applyProperties(properties);
        Installer.phase("startup").log("Loaded properties: {}", properties);

        // Liteloader Installer
        String userHome = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File mcDir;
        String mcDirName = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null) {
            mcDir = new File(System.getenv("APPDATA"), mcDirName);
        } else if (osType.contains("mac")) {
            mcDir = new File(new File(new File(userHome, "Library"), "Application Support"), "minecraft");
        } else {
            mcDir = new File(userHome, mcDirName);
        }
        //

        Installer.properties().mcDir = mcDir;
        Installer.logMessage("Set minecraft home dir to {}", mcDir);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame();
            frame.setIconImage(new ImageLayer("/installer-icon.png").resize(64, 64));
            frame.setTitle(properties.title);
            frame.setLayout(new GridBagLayout());
            frame.add(new InstallerPanel());
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
