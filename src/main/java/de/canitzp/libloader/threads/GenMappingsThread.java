package de.canitzp.libloader.threads;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.Util;
import de.canitzp.libloader.launch.NameTransformer;
import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.Mappings;
import de.canitzp.libloader.remap.MappingsParser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.util.ArrayList;
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
                ClassNode cn = classMapping.getClassNode();
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

            for(ClassMapping mapping : classMappings){
                ClassNode cn = mapping.getClassNode();
                for(String iFace : cn.interfaces){
                    if(NameTransformer.isMinecraftClass(iFace)){
                        ClassMapping iFaceMapping = Mappings.getClassMappingFromObfName(iFace);
                        if(iFaceMapping != null){
                            for(ChildMapping<MethodNode> method : mapping.getMethods()){
                                for(ChildMapping<MethodNode> interfaceMethod : iFaceMapping.getMethods()){
                                    if(method.getObfuscatedName().equals(interfaceMethod.getObfuscatedName()) && method.getObfuscatedDesc().equals(interfaceMethod.getObfuscatedDesc()) && interfaceMethod.getMappedName() != null){
                                        method.setMapped(interfaceMethod.getMappedName(), interfaceMethod.getMappedDesc());
                                    }
                                }
                            }
                        }
                    }
                }
                if(cn.superName != null){
                    List<ClassMapping> supers = new ArrayList<>();
                    ClassMapping mostParent = null;
                    while (cn != null && cn.superName != null){
                        ClassMapping mp = Mappings.getClassMappingFromName(cn.superName);
                        if(mp != null){
                            mostParent = mp;
                            supers.add(mp);
                            cn = mp.getClassNode();
                        } else {
                            cn = null;
                        }
                    }
                    if(mostParent != null){
                        for(ClassMapping mapping1 : supers){
                            if(mapping1 != mostParent){
                                for(ChildMapping<MethodNode> method : mostParent.getMethods()){
                                    for(ChildMapping<MethodNode> interfaceMethod : mapping1.getMethods()){
                                        if(method.getObfuscatedName().equals(interfaceMethod.getObfuscatedName()) && method.getObfuscatedDesc().equals(interfaceMethod.getObfuscatedDesc()) && interfaceMethod.getMappedName() != null){
                                            method.setMapped(interfaceMethod.getMappedName(), interfaceMethod.getMappedDesc());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            new MappingsParser(new File(".", "remapped.mappings")).write(classMappings);
            LibLoader.setComponentsEnableStatus(true, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn, LibLoader.mainFrame.comparableVersion);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
