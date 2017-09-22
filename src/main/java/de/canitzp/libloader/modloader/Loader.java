package de.canitzp.libloader.modloader;

import com.sun.javafx.UnmodifiableArrayList;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author canitzp
 */
public class Loader {

    private static final Map<String, Mod> loadedMods = new HashMap<>();
    private static final List<ITweaker> loadedTransformer = new ArrayList<>();

    public static void findMods(File modsDir) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File[] dirContents = modsDir.listFiles();
        if(dirContents != null){
            for(File file : dirContents){ // add to class loader
                Launch.classLoader.addURL(file.toURI().toURL());
            }
            for(File file : dirContents){ // finds actual mods
                JarFile jar = new JarFile(file);
                for(JarEntry entry : Collections.list(jar.entries())){
                    if(entry.getName().endsWith(".class") && !entry.getName().contains("$")){
                        Class clazz = Class.forName(entry.getName().replace(".class", ""), false, Launch.classLoader);
                        if(clazz.isAnnotationPresent(Mod.class)){
                            Mod mod = (Mod) clazz.getAnnotation(Mod.class);
                            loadedMods.put(mod.modid(), mod);
                        }
                        if(ITweaker.class.isAssignableFrom(clazz)){
                            loadedTransformer.add((ITweaker) clazz.newInstance());
                        }
                    }
                }
            }
        }
    }

    public static UnmodifiableArrayList<ITweaker> getTweaker(){
        return new UnmodifiableArrayList<>(loadedTransformer.toArray(new ITweaker[0]), loadedTransformer.size());
    }

}
