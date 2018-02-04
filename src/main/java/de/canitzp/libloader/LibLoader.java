package de.canitzp.libloader;

import com.google.gson.annotations.SerializedName;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import de.canitzp.libloader.remap.mappings.ClientMappings;
import de.canitzp.libloader.remap.mappings.CommonMappings;
import de.canitzp.libloader.remap.mappings.MappingsBase;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author canitzp
 */
public class LibLoader {
    public static final String VERSION = "1.3.0";
    public static final String MEDDLE_VERSION = "1.3";
    public static final String DYNAMICMAPPINGS_VERSION = "028";
    public static final String MEDDLEAPI_VERSION = "1.0.7";
    public static final String MINECRAFT_VERSIONS_MAVEN = "http://s3.amazonaws.com/Minecraft.Download/versions/";
    public static final String FYBEROPTICS_MAVEN = "http://fybertech.net/maven/net/fybertech/";
    public static PrintStream writer = new PrintStream(new OutputStream() {
        public void write(int b) throws IOException {
            mainFrame.logAreaDownload.append(String.valueOf((char)b));
            mainFrame.logAreaMappings.append(String.valueOf((char)b));
            LibLoader.oldOutStream.write(b);
        }
    });
    public static PrintStream oldOutStream;

    public static MainFrame mainFrame;
    public static Font font = Font.getFont(Font.MONOSPACED);
    public static final List<Class<? extends MappingsBase>> MAPPINGS_CLASSES = Arrays.asList(
            CommonMappings.class, ClientMappings.class
    );

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new WindowsLookAndFeel());
        UIManager.put("ProgressBar.foreground", new Color(19, 169, 30));
        UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        oldOutStream = System.out;
        System.setOut(writer);
        System.setErr(writer);

        mainFrame = new MainFrame();
    }

    public static void startThread(Thread thread){
        thread.setDaemon(true);
        try {
            thread.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setComponentsEnableStatus(boolean enable, JComponent... comps){
        for(JComponent comp : comps){
            comp.setEnabled(enable);
        }
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
