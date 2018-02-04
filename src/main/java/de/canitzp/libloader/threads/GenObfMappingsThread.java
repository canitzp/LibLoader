package de.canitzp.libloader.threads;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.Util;
import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.Mappings;
import de.canitzp.libloader.remap.script.ScriptInterpreter;
import de.canitzp.mappings.ClassPool;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class GenObfMappingsThread extends Thread {

    @Override
    public void run() {
        LibLog.infoSilence("Generate obfuscated mappings file button pressed");
        LibLoader.setComponentsEnableStatus(false, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
        LibLog.infoSilence("Open and analyzing jar file");
        File jarFile = new File(LibLoader.mainFrame.chooseJarPath.getText());
        List<String> mappings = new ArrayList<String>(){{
            add("test.js");
        }};
        try {
            Pair<List<ClassMapping>, Map<String, byte[]>> obfJar = Util.analyzeJarFile(jarFile, Util.readJarFile(jarFile));
            //ScriptInterpreter.clean();
            //ScriptInterpreter.read(new File("C:\\Users\\canitzp\\Documents\\Dev\\LibLoader\\test.txt"));
            //ScriptInterpreter.processMappings(obfJar.getKey());
            obfJar.getKey().forEach(ClassPool::add);
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval("var OPC = Java.type('org.objectweb.asm.Opcodes');");
            engine.eval("var ClassPool = Java.type('de.canitzp.mappings.ClassPool');");


            engine.eval(new InputStreamReader(LibLoader.class.getResourceAsStream("/mappings/test.js")));
        } catch (IOException | ScriptException e) {
            e.printStackTrace();
        }

        try {
            //Mappings.classMappings = obfJar.getKey();
            //new MappingsParser(new File(".", "obfuscated.mappings")).write(obfJar.getKey());

            //Mappings.classMappings.forEach(classMapping -> classMapping.getFields().stream().filter(fieldNodeChildMapping -> fieldNodeChildMapping.getMatchProbability(Opcodes.ACC_FINAL, "Lcom/mojang/authlib/properties/PropertyMap;", 1, -1) >= 1.0).forEach(fieldNodeChildMapping -> System.out.println(fieldNodeChildMapping.getParent().getObfName())));


            LibLoader.setComponentsEnableStatus(true, LibLoader.mainFrame.generateObfuscatedMappingsFileBtn, LibLoader.mainFrame.generateMappedMappingsFileBtn, LibLoader.mainFrame.applyMappingsToJarBtn, LibLoader.mainFrame.chooseJarBtn);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
