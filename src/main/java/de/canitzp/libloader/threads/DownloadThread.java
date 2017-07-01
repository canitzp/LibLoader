package de.canitzp.libloader.threads;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.VersionFrame;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author canitzp
 */
public class DownloadThread extends Thread {

    public DownloadThread() {
        this.setDaemon(true);
    }

    public void run() {
        LibLoader.mainFrame.chooseVersionBtn.setEnabled(false);
        try {
            String mcVersion = VersionFrame.choosenVersion;
            File cacheDir = new File(getExecutionPath(), "cache");
            File libsDir = new File(cacheDir, "libraries");
            File client = new File(cacheDir, mcVersion + ".jar");
            File server = new File(cacheDir, "minecraft_server." + mcVersion + ".jar");
            File json = new File(cacheDir, mcVersion + ".json");
            log("=== LibLoader " + LibLoader.VERSION + " ===");
            log("Minecraft version: " + mcVersion);
            log("Destination: " + cacheDir.getAbsolutePath());
            cacheDir.mkdirs();
            log("> Downloading Minecraft...");
            if (!client.exists()) {
                log(">> Download Client");
                this.downloadFile(LibLoader.MINECRAFT_VERSIONS_MAVEN + mcVersion + "/" + client.getName(), client);
            } else {
                log(">> Skipping Client file since it already exists");
            }

            if (!server.exists()) {
                log(">> Download Server");
                this.downloadFile(LibLoader.MINECRAFT_VERSIONS_MAVEN + mcVersion + "/" + server.getName(), server);
            } else {
                log(">> Skipping Server file since it already exists");
            }

            if (!json.exists()) {
                log(">> Download Json");
                FileUtils.copyURLToFile(new URL(LibLoader.MINECRAFT_VERSIONS_MAVEN + mcVersion + "/" + json.getName()), json);
            } else {
                log(">> Skipping Json file since it already exists");
            }

            log(">> Download Libraries");
            libsDir.mkdirs();
            Gson gson = new Gson();
            LibLoader.JSONData data = gson.fromJson(new JsonReader(new FileReader(json)), LibLoader.JSONData.class);
            Iterator var9 = data.libraries.iterator();

            while(true) {
                while(var9.hasNext()) {
                    LibLoader.JSONDataLib libData = (LibLoader.JSONDataLib)var9.next();
                    LibLoader.JSONDataLibArtifacts art = libData.downloads.artifact;
                    if (art != null && art.url != null) {
                        downloadArtifact(libsDir, art, false);
                    } else {
                        LibLoader.JSONDataLibClassifier classifier = libData.downloads.classifiers;
                        downloadArtifact(libsDir, classifier.linux, true);
                        downloadArtifact(libsDir, classifier.mac, true);
                        downloadArtifact(libsDir, classifier.win, true);
                    }
                }

                log("> Downloading Meddle v" + LibLoader.MEDDLE_VERSION);
                File meddle = new File(libsDir, "meddle-" + LibLoader.MEDDLE_VERSION + ".jar");
                if (!meddle.exists()) {
                    FileUtils.copyURLToFile(new URL(LibLoader.FYBEROPTICS_MAVEN + "meddle/" + LibLoader.MEDDLE_VERSION + "/meddle-" + LibLoader.MEDDLE_VERSION + ".jar"), meddle);
                }

                log("> Downloading DynamicMappings v" + LibLoader.DYNAMICMAPPINGS_VERSION);
                File dyn = new File(libsDir, "dynamicmappings-" + LibLoader.DYNAMICMAPPINGS_VERSION + ".jar");
                if (!dyn.exists()) {
                    FileUtils.copyURLToFile(new URL(LibLoader.FYBEROPTICS_MAVEN + "dynamicmappings/" + LibLoader.DYNAMICMAPPINGS_VERSION + "/dynamicmappings-" + LibLoader.DYNAMICMAPPINGS_VERSION + ".jar"), dyn);
                }

                log("> Downloading MeddleAPI v" + LibLoader.MEDDLEAPI_VERSION);
                File api = new File(libsDir, "meddleapi-" + LibLoader.MEDDLEAPI_VERSION + ".jar");
                if (!api.exists()) {
                    FileUtils.copyURLToFile(new URL(LibLoader.FYBEROPTICS_MAVEN + "meddleapi/" + LibLoader.MEDDLEAPI_VERSION + "/meddleapi-" + LibLoader.MEDDLEAPI_VERSION + ".jar"), api);
                }

                log("> Write build.gradle file");
                File gradleFile = new File(cacheDir.getParentFile(), "build.gradle");
                if (!gradleFile.exists()) {
                    FileUtils.copyInputStreamToFile(LibLoader.class.getResourceAsStream("/build.gradle.defaults"), gradleFile);
                }

                File srcFolder = new File(cacheDir.getParentFile(), "src" + File.separator + "main");
                if (!srcFolder.exists()) {
                    srcFolder.mkdirs();
                    (new File(srcFolder, "java" + File.separator + "com" + File.separator + "exampleguy" + File.separator + "examplemod")).mkdirs();
                    (new File(srcFolder, "resources" + File.separator + "assets" + File.separator + "examplemod")).mkdirs();
                }

                File gradleWrapper = new File(cacheDir.getParentFile(), "gradle" + File.separator + "wrapper");
                if (!gradleWrapper.exists()) {
                    gradleWrapper.mkdirs();
                    FileUtils.copyInputStreamToFile(LibLoader.class.getResourceAsStream("/gradle-wrapper.jar"), new File(gradleWrapper, "gradle-wrapper.jar"));
                    FileUtils.copyInputStreamToFile(LibLoader.class.getResourceAsStream("/gradle-wrapper.properties"), new File(gradleWrapper, "gradle-wrapper.properties"));
                    FileUtils.copyInputStreamToFile(LibLoader.class.getResourceAsStream("/gradlew"), new File(cacheDir.getParentFile(), "gradlew"));
                    FileUtils.copyInputStreamToFile(LibLoader.class.getResourceAsStream("/gradlew.bat"), new File(cacheDir.getParentFile(), "gradlew.bat"));
                }

                log("> Finished setup");
                log("Now you can run 'gradlew build' in a terminal and hopefully everything setups fine.");
                break;
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        }

        LibLoader.mainFrame.chooseVersionBtn.setEnabled(true);
    }

    private void downloadFile(String urlString, File file) {
        try {
            URL url = new URL(urlString);
            FileUtils.copyURLToFile(url, file);
        } catch (MalformedURLException var4) {
            var4.printStackTrace();
        } catch (IOException var5) {
            log("Could't download file.");
        }

    }

    private static void downloadArtifact(File libDir, LibLoader.JSONDataLibArtifacts artifact, boolean extract) throws IOException {
        if (artifact == null) {
            log(">>> Skip a file, cause there isn't a download available.");
        } else {
            String[] split = artifact.url.split("/");
            File file = new File(libDir, split[split.length - 1]);
            if (!file.exists()) {
                log(">>> Download " + file.getName());
                FileUtils.copyURLToFile(new URL(artifact.url), file);
            } else {
                log(">>> Skip " + file.getName());
            }

            if (extract) {
                File dest = new File(libDir, "natives");
                ZipFile zip = new ZipFile(file);
                dest.mkdirs();
                Enumeration entries = zip.entries();

                while(true) {
                    ZipEntry entry;
                    do {
                        if (!entries.hasMoreElements()) {
                            zip.close();
                            return;
                        }

                        entry = (ZipEntry)entries.nextElement();
                    } while(entry.isDirectory());

                    InputStream stream = zip.getInputStream(entry);
                    int pos = 0;
                    byte[] data = new byte[(int)entry.getSize()];

                    int read;
                    do {
                        read = stream.read(data, pos, Math.min(1024, (int)entry.getSize() - pos));
                        pos += read;
                    } while(read >= 1);

                    if (!entry.getName().endsWith(".MF")) {
                        File outFile = new File(dest, entry.getName());
                        if (!outFile.exists()) {
                            log(">>> Extract native " + entry.getName());
                            outFile.toPath().getParent().toFile().mkdirs();
                            FileOutputStream out = new FileOutputStream(outFile);
                            out.write(data);
                            out.close();
                        }
                    }
                }
            }
        }
    }

    public static void log(String msg) {
        System.out.println(msg);
    }

    public static File getExecutionPath() throws UnsupportedEncodingException {
        String path = URLDecoder.decode(LibLoader.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
        return (new File(path)).getParentFile().getAbsoluteFile();
    }
}
