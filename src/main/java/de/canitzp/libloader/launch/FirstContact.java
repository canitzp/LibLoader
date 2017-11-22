package de.canitzp.libloader.launch;

import de.canitzp.libloader.remap.CustomRemapper;
import de.canitzp.libloader.remap.MappingsParser;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;

/**
 * @author canitzp
 */
public class FirstContact {

    public static MCClassLoader classLoader;
    public static File gameDir, assetsDir;
    public static String version;

    public static void main(String[] args){
        classLoader = new MCClassLoader(((URLClassLoader) FirstContact.class.getClassLoader()).getURLs());
        Thread.currentThread().setContextClassLoader(classLoader);


        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSet set = parser.parse(args);
        version = set.valueOf(parser.accepts("version").withRequiredArg());
        gameDir = set.valueOf(parser.accepts("gameDir").withRequiredArg().ofType(File.class));
        assetsDir = set.valueOf(parser.accepts("assetsDir").withRequiredArg().ofType(File.class));

        try {
            MCClassLoader.mappings = new MappingsParser(new File(gameDir,"loader" + File.separator + "remapped.mappings")).read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class clazz = classLoader.findClass("net.minecraft.client.main.Main");
            clazz.getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
