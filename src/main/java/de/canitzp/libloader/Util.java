package de.canitzp.libloader;

import de.canitzp.libloader.remap.ClassMapping;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author canitzp
 */
public class Util {

    public static List<JarEntry> readJarFile(File jarFile) throws IOException {
        JarFile jar = new JarFile(jarFile);
        return Collections.list(jar.entries());
    }

    public static Pair<List<ClassMapping>, Map<String, byte[]>> analyzeJarFile(File jarFile, List<JarEntry> entries) throws IOException {
        List<ClassMapping> classMappings = new ArrayList<>();
        Map<String, byte[]> nonClassResources = new HashMap<>();
        JarFile jar = new JarFile(jarFile);
        for(JarEntry entry : entries){
            if(entry.getName().endsWith(".class")){
                ClassMapping classMapping = new ClassMapping();
                classMapping.setObfName(entry.getName().substring(0, entry.getName().length() - 6));
                classMapping.setClassReader(new ClassReader(jar.getInputStream(entry)));
                classMappings.add(classMapping);
            } else {
                nonClassResources.put(entry.getName(), IOUtils.toByteArray(jar.getInputStream(entry)));
            }
        }
        return Pair.of(classMappings, nonClassResources);
    }

}
