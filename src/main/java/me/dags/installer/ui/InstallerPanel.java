package me.dags.installer.ui;

import javafx.util.Pair;
import me.dags.installer.Installer;
import me.dags.installer.Launcher;
import me.dags.installer.Tooltips;
import me.dags.installer.Versions;
import me.dags.installer.task.ForgeInstall;
import me.dags.installer.task.ModpackInstall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

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
        final int windowWidth = 550;
        final int buttonWidth = 75;
        final int rowHeight = 23;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.installDir = new File(Installer.properties().mcDir, "profiles");

        try {
            List<String> backgrounds = scanBackgrounds();
            if (backgrounds.size() > 0) {
                String name = backgrounds.get(new Random().nextInt(backgrounds.size()));
                ImageLayer background = new ImageLayer("/backgrounds/" + name, 0.1).scale(1.05).setCover(true);
                ImageLayer icon = new ImageLayer("/icon_128.png", 0.05).scale(0.7).margins(-20, 0);
                ImageLayer logo = new ImageLayer("/ac-text.png", 0.05).scale(0.65).margins(50, 10);

                ParallaxLayers banner = new ParallaxLayers();
                banner.setPreferredSize(new Dimension(windowWidth, 260));
                banner.addLayer(background);
                banner.addLayer(icon);
                banner.addLayer(logo);
                this.add(banner);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        forgeVersions.setPreferredSize(new Dimension(windowWidth - (buttonWidth * 2), rowHeight));
        forgeVersions.setToolTipText(Tooltips.FORGE_VERSION);
        forgeInstall.setPreferredSize(new Dimension((buttonWidth * 2), rowHeight));
        forgeInstall.setToolTipText(Tooltips.FORGE_INSTALL);
        forgeInstall.addActionListener(e -> new Thread(new ForgeInstall(this)).start());

        targetDir.setPreferredSize(new Dimension(windowWidth - (buttonWidth * 2), rowHeight));
        targetDir.setText(installDir.getAbsolutePath());
        targetSelect.setPreferredSize(new Dimension((buttonWidth * 2), rowHeight));
        targetSelect.setToolTipText(Tooltips.INSTALL_DIR);
        targetSelect.addActionListener(fileSelection());

        install.setSelected(true);
        install.addActionListener(radioInverter(install, extract));
        install.setToolTipText(Tooltips.INSTALL_OPT);
        extract.setSelected(false);
        extract.addActionListener(radioInverter(extract, install));
        extract.setToolTipText(Tooltips.EXTRACT_OPT);

        run.setPreferredSize(new Dimension(buttonWidth, rowHeight));
        run.setEnabled(false);
        run.addActionListener(e -> new Thread(new ModpackInstall(this)).start());
        close.setPreferredSize(new Dimension(buttonWidth, rowHeight));
        close.addActionListener(e -> System.exit(0));

        ToolTipManager.sharedInstance().setInitialDelay(1);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

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
            forgeVersions.setEnabled(false);
            forgeVersions.addItem("No Forge installation detected");
        } else {
            forgeVersions.setEnabled(true);
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

    private static List<String> scanBackgrounds() throws IOException {
        List<String> backgrounds = new ArrayList<>();
        InputStream inputStream = Launcher.class.getResourceAsStream("/backgrounds");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String name;
        while ((name = reader.readLine()) != null) {
            backgrounds.add(name);
        }
        reader.close();
        inputStream.close();
        return backgrounds;
    }
}
