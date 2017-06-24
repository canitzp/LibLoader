package de.canitzp.libloader;

import com.google.gson.annotations.SerializedName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * @author canitzp
 */
public class LibLoader {
    public static final String VERSION = "1.1.0";
    public static final String MEDDLE_VERSION = "1.3";
    public static final String DYNAMICMAPPINGS_VERSION = "028";
    public static final String MEDDLEAPI_VERSION = "1.0.7";
    public static final String MINECRAFT_VERSIONS_MAVEN = "http://s3.amazonaws.com/Minecraft.Download/versions/";
    public static final String FYBEROPTICS_MAVEN = "http://fybertech.net/maven/net/fybertech/";
    public static DownloadThread downloadThread;
    public static JTextArea area = new JTextArea();
    public static JButton close = new JButton("Close");
    public static JButton versions = new JButton("Choose Version");
    public static PrintStream writer = new PrintStream(new OutputStream() {
        public void write(int b) throws IOException {
            LibLoader.area.append(String.valueOf((char)b));
            LibLoader.oldOutStream.write(b);
        }
    });
    public static PrintStream oldOutStream;

    public static void main(String[] args) throws Exception {
        oldOutStream = System.out;
        System.setOut(writer);
        System.setErr(writer);
        JFrame frame = new JFrame("LibLoader " + VERSION + " by canitzp");
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(600, 400));
        frame.add(versions, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(close, BorderLayout.SOUTH);
        close.setEnabled(false);
        close.addActionListener((e) -> frame.dispose());
        area.setEditable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        versions.addActionListener((e) -> {
            try {
                new VersionFrame(frame);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (LibLoader.downloadThread != null) {
                    LibLoader.downloadThread.stop();
                }
            }
        });
    }

    public static class JSONDataLibClassifier {
        @SerializedName("natives-linux")
        public LibLoader.JSONDataLibArtifacts linux;
        @SerializedName("natives-osx")
        public LibLoader.JSONDataLibArtifacts mac;
        @SerializedName("natives-windows")
        public LibLoader.JSONDataLibArtifacts win;

        public JSONDataLibClassifier() {
        }
    }

    public static class JSONDataLibArtifacts {
        public int size;
        public String sha1;
        public String path;
        public String url;

        public JSONDataLibArtifacts() {
        }
    }

    public static class JSONDataLibDownloads {
        public LibLoader.JSONDataLibClassifier classifiers;
        public LibLoader.JSONDataLibArtifacts artifact;

        public JSONDataLibDownloads() {
        }
    }

    public static class JSONDataLib {
        public String name;
        public LibLoader.JSONDataLibDownloads downloads;

        public JSONDataLib() {
        }
    }

    public static class JSONData {
        public Object assetIndex;
        public String assets;
        public Object downloads;
        public String id;
        public List<JSONDataLib> libraries;

        public JSONData() {
        }
    }
}
