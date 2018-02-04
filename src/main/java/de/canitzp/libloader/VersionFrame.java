package de.canitzp.libloader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import de.canitzp.libloader.threads.DownloadThread;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

/**
 * @author canitzp
 */
public class VersionFrame extends JFrame {

    private JFrame parent;
    public static String choosenVersion;

    public VersionFrame(JFrame parent) throws Exception {
        this.parent = parent;
        this.run();
    }

    private void run() throws Exception {
        final JList<String> versions = new JList<>();
        File version = new File(FileUtils.getTempDirectory(), "versions.json.temp");
        FileUtils.copyURLToFile(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), version);
        VersionFrame.VersionJSON json = new Gson().fromJson(new JsonReader(new FileReader(version)), VersionJSON.class);
        final DefaultListModel<String> model = new DefaultListModel<>();

        for (VersionMC mcversion : json.versions) {
            model.addElement(mcversion.gameVersion);
        }

        versions.setModel(model);
        this.add(new JScrollPane(versions));
        versions.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!e.isConsumed() && e.getClickCount() == 2) {
                    VersionFrame.this.dispose();
                    VersionFrame.choosenVersion = model.get(versions.locationToIndex(e.getPoint()));
                    LibLoader.mainFrame.clearLog();
                    new DownloadThread().start();
                }
            }
        });
        this.setMinimumSize(new Dimension(360, 480));
        this.setMaximumSize(this.getMinimumSize());
        this.setResizable(false);
        this.setLocationRelativeTo(this.parent);
        this.pack();
        this.setVisible(true);
    }

    public class VersionMC {
        @SerializedName("id")
        public String gameVersion;
        public String type;
        public String time;
        public String releaseTime;
        public URL url;

        public VersionMC() {
        }
    }

    public class VersionJSON {
        public Object latest;
        public List<VersionMC> versions;

        public VersionJSON() {
        }
    }
}
