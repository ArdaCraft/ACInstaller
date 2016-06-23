package me.dags.installer.task;

import javafx.util.Pair;
import me.dags.installer.Installer;
import me.dags.installer.InstallerPanel;
import me.dags.installer.process.DownloadProcess;
import me.dags.installer.process.UnzipProcess;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
public class ModpackInstall implements Runnable {

    private final InstallerPanel installerPanel;
    private Path installPath;
    private JFrame frame;
    private JProgressBar progressBar;

    public ModpackInstall(InstallerPanel panel) {
        this.installerPanel = panel;
        Pair<JFrame, JProgressBar> pair = installerPanel.progressBar();
        this.frame = pair.getKey();
        this.progressBar = pair.getValue();
    }

    @Override
    public void run() {
        Installer.phase("Modpack Download");

        frame.setTitle("Downloading...");
        progressBar.setValue(0);
        progressBar.setMinimum(0);

        ModpackFetcher grabber = new ModpackFetcher(Installer.properties().github.user, Installer.properties().github.repo);
        grabber.run();

        installPath = installerPanel.installDir.toPath().resolve(Installer.properties().profile_name + "-" + grabber.tag());
        Path target = installPath.getParent().resolve(grabber.fileName());

        DownloadProcess downloadProcess = DownloadProcess.builder()
                .url(grabber.url())
                .path(target)
                .attemptInterval(500L)
                .attempts(5)
                .lengthConsumer(progressBar::setMaximum)
                .progressConsumer(progressBar::setValue)
                .completionConsumer(this::extract)
                .build();

        new Thread(downloadProcess).start();
    }

    private void extract(Path path) {
        Installer.phase("Modpack Extract");

        frame.setTitle("Installing...");
        progressBar.setValue(0);
        progressBar.setMinimum(0);

        UnzipProcess unzipProcess = UnzipProcess.builder()
                .zip(path)
                .target(installPath)
                .lengthConsumer(progressBar::setMaximum)
                .progressConsumer(progressBar::setValue)
                .completionConsumer(this::finish)
                .build();

        new Thread(unzipProcess).run();
    }

    private void finish(Path path) {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (installerPanel.installProfile()) {
            try {
                Thread.sleep(500L);
                frame.setTitle("Installing Profile...");
                new ProfileInstall(installerPanel.extendProfile(), installPath).run();
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        frame.dispose();
        installerPanel.lock();
    }
}
