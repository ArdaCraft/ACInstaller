package me.dags.installer.ui;

import javafx.util.Pair;
import me.dags.installer.Installer;
import me.dags.installer.Versions;
import me.dags.installer.task.ForgeInstall;
import me.dags.installer.task.ModpackInstall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public class InstallerPanel extends JPanel {

    private final JComboBox<String> forgeVersions = new JComboBox<>();
    private final JButton forgeInstall = new JButton("Forge Installer");
    private final JTextField targetDir = new JTextField();
    private final JButton targetSelect = new JButton("Select Directory");
    private final JRadioButton install = new JRadioButton("Install");
    private final JRadioButton extract = new JRadioButton("Extract");
    private final JButton run = new JButton("Run");
    private final JButton close = new JButton("Close");

    public File installDir = new File("");

    public InstallerPanel() {
        final int windowWidth = 500;
        final int buttonWidth = 75;
        final int rowHeight = 23;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.installDir = new File(Installer.properties().mcDir, "profiles");

        try {
            ImageResource background = new ImageResource("/installer-banner.png");
            ParallaxImage banner = new ParallaxImage(background, 0.15D);
            banner.setPreferredSize(new Dimension(windowWidth, 260));

            ImageResource overlay = new ImageResource("/installer-logo.png").scale(0.5);
            banner.setOverlay(overlay, -1, -1, overlay.getWidth(), overlay.getHeight());

            this.add(banner);
        } catch (IOException e) {
            e.printStackTrace();
        }

        forgeVersions.setPreferredSize(new Dimension(windowWidth - (buttonWidth * 2), rowHeight));
        forgeVersions.setToolTipText("Select a Forge installation to extend");
        forgeInstall.setPreferredSize(new Dimension((buttonWidth * 2), rowHeight));
        forgeInstall.setToolTipText("Download and launch the recommended Forge installer");
        forgeInstall.addActionListener(e -> new Thread(new ForgeInstall(this)).start());

        targetDir.setPreferredSize(new Dimension(windowWidth - (buttonWidth * 2), rowHeight));
        targetDir.setText(installDir.getAbsolutePath());
        targetSelect.setPreferredSize(new Dimension((buttonWidth * 2), rowHeight));
        targetSelect.setToolTipText("Select where to install/extract the modpack files to");
        targetSelect.addActionListener(fileSelection());

        install.setSelected(true);
        install.addActionListener(radioInverter(install, extract));
        install.setToolTipText("Extract the modpack to the target directory and add a new profile to the Vanilla launcher");
        extract.setSelected(false);
        extract.addActionListener(radioInverter(extract, install));
        extract.setToolTipText("Extract the modpack to the target directory");

        run.setPreferredSize(new Dimension(buttonWidth, rowHeight));
        run.setEnabled(false);
        run.addActionListener(e -> new Thread(new ModpackInstall(this)).start());
        close.setPreferredSize(new Dimension(buttonWidth, rowHeight));
        close.addActionListener(e -> System.exit(0));

        this.add(toRow(forgeVersions, forgeInstall));
        this.add(toRow(targetDir, targetSelect));
        this.add(toRow(install, extract, run, close));

        updateVersions();
        updateInstallMode();
    }

    public boolean installProfile() {
        return install.isEnabled() && install.isSelected() && !extendProfile().isEmpty();
    }

    public String extendProfile() {
        return forgeVersions.getSelectedItem().toString();
    }

    private void updateInstallMode() {
        if (install.isEnabled() && install.isSelected()) {
            run.setEnabled(forgeVersions.isEnabled());
        }
        if (extract.isEnabled() && extract.isSelected()) {
            run.setEnabled(true);
        }
    }

    public void updateVersions() {
        forgeVersions.removeAllItems();
        Collection<String> versions = new Versions().getVersions();
        if (versions.isEmpty()) {
            forgeInstall.setEnabled(true);
            forgeVersions.setEnabled(false);
            forgeVersions.addItem("No Forge installation detected");
        } else {
            forgeVersions.setEnabled(true);
            forgeInstall.setEnabled(false);
            versions.forEach(forgeVersions::addItem);
        }
        updateInstallMode();
    }

    public void lock() {
        forgeVersions.setEnabled(false);
        targetSelect.setEnabled(false);
        install.setEnabled(false);
        extract.setEnabled(false);
        run.setEnabled(false);
    }

    public void unlock() {
        targetSelect.setEnabled(true);
        install.setEnabled(true);
        extract.setEnabled(true);
        updateVersions();
    }

    public Pair<JFrame, JProgressBar> progressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(350, 30));

        JFrame frame = new JFrame();
        frame.setLayout(new GridBagLayout());
        frame.add(progressBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        return new Pair<>(frame, progressBar);
    }

    private ActionListener radioInverter(JRadioButton target, JRadioButton other) {
        return e -> {
            other.setSelected(!target.isSelected());
            updateInstallMode();
        };
    }

    private ActionListener fileSelection() {
        return e -> {
            if (!targetDir.getText().equals(installDir.getAbsolutePath())) {
                installDir = new File(targetDir.getText());
            }

            if (!installDir.exists() && installDir.mkdirs()) ;

            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(installDir);
            dirChooser.setSelectedFile(installDir);

            int response = dirChooser.showOpenDialog(InstallerPanel.this);

            if (response == JFileChooser.APPROVE_OPTION) {
                installDir = dirChooser.getSelectedFile();
                targetDir.setText(installDir.getAbsolutePath());
                Installer.phase("settings").log("Set installation dir to {}", installDir);
            }
        };
    }

    private JPanel toRow(Component... component) {
        JPanel row = new JPanel();
        for (Component c : component) {
            row.add(c);
        }
        return row;
    }
}
