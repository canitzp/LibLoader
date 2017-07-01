package de.canitzp.libloader.threads;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.MainFrame;
import de.canitzp.libloader.Util;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.CustomClassRemapper;
import de.canitzp.libloader.remap.CustomRemapper;
import de.canitzp.libloader.remap.MappingsParser;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author canitzp
 */
public class ApplyMappingsThread extends Thread {

    @Override
    public void run() {
        LibLog.infoSilence("Apply mappings to jar button pressed");
        try {
            JProgressBar bar = LibLoader.mainFrame.mappingsProgress;
            JFileChooser chooser = new JFileChooser(LibLoader.mainFrame.chooseJarPath.getText());
            chooser.setDialogTitle("Choose Mappings File");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Minecraft Mappings-File (.mappings)", "mappings"));
            if (chooser.showOpenDialog(LibLoader.mainFrame) == JFileChooser.APPROVE_OPTION) {
                LibLoader.setComponentsEnableStatus(false, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
                LibLog.infoSilence("Reading mappings file");
                List<ClassMapping> mappings = new MappingsParser(chooser.getSelectedFile()).read();
                File jar = new File(LibLoader.mainFrame.chooseJarPath.getText());
                CustomRemapper remapper = new CustomRemapper(mappings);
                JarOutputStream jos = new JarOutputStream(new FileOutputStream(new File(jar.getParentFile(), "remapped-" + jar.getName())));
                LibLog.infoSilence("Analyze Minecraft jar");
                Pair<List<ClassMapping>, Map<String, byte[]>> pair = Util.analyzeJarFile(jar, Util.readJarFile(jar));
                LibLog.infoSilence("Merge class reader");
                for(ClassMapping oldMapping : pair.getKey()){
                    for(ClassMapping mapping : mappings){
                        if(mapping.getObfName().equals(oldMapping.getObfName())){
                            mapping.setClassReader(oldMapping.getClassReader());
                            break;
                        }
                    }
                }
                LibLog.infoSilence("Remap class files");
                bar.setMaximum(mappings.size() - 1);
                bar.setStringPainted(true);
                for(ClassMapping mapping : mappings){
                    bar.setString("Remap " + (mapping.getMappedName() != null ? mapping.getMappedName() : mapping.getObfName()));
                    bar.setValue(bar.getValue() + 1);
                    ClassWriter cw = new ClassWriter(0);
                    mapping.getClassReader().accept(new CustomClassRemapper(cw, remapper), ClassReader.EXPAND_FRAMES);
                    jos.putNextEntry(new ZipEntry((mapping.getMappedName() != null ? mapping.getMappedName() : mapping.getObfName()) + ".class"));
                    jos.write(cw.toByteArray());
                }
                LibLog.infoSilence("Process non class files");
                bar.setMaximum(pair.getValue().size() - 1);
                bar.setValue(0);
                for(Map.Entry<String, byte[]> nonClassFiles : pair.getValue().entrySet()){
                    bar.setString("Process " + nonClassFiles.getKey());
                    bar.setValue(bar.getValue() + 1);
                    jos.putNextEntry(new JarEntry(nonClassFiles.getKey()));
                    jos.write(nonClassFiles.getValue());
                }
                bar.setValue(0);
                bar.setString("");
                jos.close();
                LibLog.infoSilence("Remapped jar finished.");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        LibLoader.setComponentsEnableStatus(true, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
    }
}