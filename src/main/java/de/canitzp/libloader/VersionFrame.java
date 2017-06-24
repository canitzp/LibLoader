package de.canitzp.libloader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Iterator;
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
        final JList<String> versions = new JList();
        File version = new File(FileUtils.getTempDirectory(), "versions.json.temp");
        FileUtils.copyURLToFile(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), version);
        VersionFrame.VersionJSON json = (VersionFrame.VersionJSON)(new Gson()).fromJson(new JsonReader(new FileReader(version)), VersionFrame.VersionJSON.class);
        final DefaultListModel<String> model = new DefaultListModel();
        Iterator var5 = json.versions.iterator();

        while(var5.hasNext()) {
            VersionFrame.VersionMC mcversion = (VersionFrame.VersionMC)var5.next();
            model.addElement(mcversion.gameVersion);
        }

        versions.setModel(model);
        this.add(new JScrollPane(versions));
        versions.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!e.isConsumed() && e.getClickCount() == 2) {
                    VersionFrame.this.dispose();
                    LibLoader.versions.setEnabled(false);
                    VersionFrame.choosenVersion = (String)model.get(versions.locationToIndex(e.getPoint()));
                    LibLoader.area.setText("");
                    LibLoader.area.setCaretPosition(LibLoader.area.getDocument().getLength());
                    LibLoader.downloadThread = new DownloadThread();
                    LibLoader.downloadThread.start();
                }

            }
        });
        this.setMinimumSize(new Dimension(400, 500));
        this.setMaximumSize(this.getMinimumSize());
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
