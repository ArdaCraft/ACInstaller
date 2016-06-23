package me.dags.installer.task;

import javafx.util.Pair;
import me.dags.installer.Installer;
import me.dags.installer.InstallerPanel;
import me.dags.installer.process.DownloadProcess;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
public class ForgeInstall implements Runnable {

    private final InstallerPanel installerPanel;
    private final JFrame frame;
    private final JProgressBar progressBar;

    public ForgeInstall(InstallerPanel installerPanel) {
        Pair<JFrame, JProgressBar> pair = installerPanel.progressBar();
        this.frame = pair.getKey();
        this.progressBar = pair.getValue();
        this.installerPanel = installerPanel;
    }

    @Override
    public void run() {
        Installer.phase("Forge Install");

        frame.setTitle("Downloading Forge Installer");
        installerPanel.lock();

        ForgeFetcher grabber = new ForgeFetcher("1.8.9", "recommended");
        grabber.run();

        DownloadProcess downloadProcess = DownloadProcess.builder()
                .path(grabber.path())
                .url(grabber.url())
                .attemptInterval(500L)
                .attempts(1)
                .lengthConsumer(progressBar::setMaximum)
                .progressConsumer(progressBar::setValue)
                .completionConsumer(this::launch)
                .build();

        new Thread(downloadProcess).run();
    }

    public void launch(Path path) {
        frame.dispose();

        try {
            Process process = Runtime.getRuntime().exec("java -jar " + path);
            while (process.isAlive()) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        installerPanel.unlock();
    }
}
