package de.canitzp.libloader.launch;

import de.canitzp.libloader.modloader.Loader;
import de.canitzp.libloader.remap.CustomRemapper;
import de.canitzp.libloader.remap.MappingsParser;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class Tweaker implements ITweaker {

    public static final boolean DEBUG = true;

    private List<String> args;
    private File loaderInternals;
    public static File debugDirectory, modsDir;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.loaderInternals = new File(gameDir, "loader");
        this.loaderInternals.mkdirs();
        modsDir = new File(gameDir, "mods");
        modsDir.mkdirs();
        debugDirectory = new File(gameDir, "debug");
        if(debugDirectory.exists()){
            try {
                FileUtils.cleanDirectory(debugDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
            debugDirectory.delete();
        }
        debugDirectory.mkdirs();
        args = new ArrayList<>(args);
        args.set(args.indexOf("--versionType") + 1, "LibLoader");
        // If they specified a custom version name, pass it to Minecraft
        if (profile != null) {
            args.add("--version");
            args.add(profile);
        }
        // If they specified an assets dir, pass it to Minecraft
        if (assetsDir != null) {
            args.add("--assetsDir");
            args.add(assetsDir.getPath());
        }
        try {
            NameTransformer.remapper = new CustomRemapper(new MappingsParser(new File(this.loaderInternals, "remapped.mappings")).read());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.args = args;

        //Load all for Mods and Tweaker
        try {
            Loader.findMods(modsDir);
            for(ITweaker tweaker : Loader.getTweaker()){
                tweaker.acceptOptions(this.args, gameDir, assetsDir, profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.registerTransformer(NameTransformer.class.getName());
        for(ITweaker tweaker : Loader.getTweaker()){
            tweaker.injectIntoClassLoader(classLoader);
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return this.args.toArray(new String[0]);
    }
}
