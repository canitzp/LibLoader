package de.canitzp.libloader.threads;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.MainFrame;
import de.canitzp.libloader.Util;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.MappingsParser;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class GenObfMappingsThread extends Thread {

    @Override
    public void run() {
        LibLog.infoSilence("Generate obfuscated mappings file button pressed");
        try {
            LibLoader.setComponentsEnableStatus(false, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
            LibLog.infoSilence("Open and analyzing jar file");
            File jarFile = new File(LibLoader.mainFrame.chooseJarPath.getText());
            Pair<List<ClassMapping>, Map<String, byte[]>> obfJar = Util.analyzeJarFile(jarFile, Util.readJarFile(jarFile));
            new MappingsParser(new File(".", "obfuscated.mappings")).write(obfJar.getKey());
            LibLoader.setComponentsEnableStatus(true, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
