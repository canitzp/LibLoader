package de.canitzp.libloader.threads;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.MainFrame;
import de.canitzp.libloader.Util;
import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.Mappings;
import de.canitzp.libloader.remap.MappingsParser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.util.List;

/**
 * @author canitzp
 */
public class GenMappingsThread extends Thread {

    @Override
    public void run() {
        LibLog.infoSilence("Generate mapped mappings file button pressed");
        try {
            LibLoader.setComponentsEnableStatus(false, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
            LibLog.infoSilence("Open and analyzing jar file");
            File jarFile = new File(LibLoader.mainFrame.chooseJarPath.getText());
            List<ClassMapping> classMappings = Mappings.classMappings = Util.analyzeJarFile(jarFile, Util.readJarFile(jarFile)).getKey();
            for(ClassMapping classMapping : classMappings){
                if(classMapping.getObfName().contains("/")){
                    classMapping.setMappedName(classMapping.getObfName());
                }
                ClassNode cn = classMapping.createClassNode();
                for(MethodNode method : cn.methods){
                    ChildMapping<MethodNode> child = new ChildMapping<MethodNode>().setObfName(method.name, method.desc).setNode(method);
                    if(method.name.equals("<init>")){
                        child.setMapped(method.name, null);
                    } else if(method.name.equals("<clinit>")){
                        child.setMapped(method.name, method.desc);
                    }
                    classMapping.addMethod(child);
                }
                for(FieldNode field : cn.fields){
                    classMapping.addField(new ChildMapping<FieldNode>().setObfName(field.name, field.desc).setNode(field));
                }
            }
            Mappings.findClassNames();

            new MappingsParser(new File(".", "remapped.mappings")).write(classMappings);
            LibLoader.setComponentsEnableStatus(true, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
