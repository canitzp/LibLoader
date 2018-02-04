package de.canitzp.libloader.remap.script;

import de.canitzp.libloader.remap.ClassMapping;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author canitzp
 */
public class ScriptInterpreter {

    private static final Map<String, List<String>> MAPPINGS = new ConcurrentHashMap<>();
    private static final List<String> RESOLVED_DEPENDENCIES = new ArrayList<>();

    public static void read(File file) throws IOException {
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        if(!lines.isEmpty()){
            String line0 = lines.get(0);
            if(!line0.isEmpty() && line0.startsWith("HEAD: ")){
                if(line0.contains("dependency=")){
                    int index = line0.indexOf("dependency=");
                    int endIndex = line0.indexOf(" ", index);
                    String dep = line0.substring(index + 11, endIndex != -1 ? endIndex : line0.length());
                    lines.remove(0);
                    List<String> list = MAPPINGS.getOrDefault(dep, new ArrayList<>());
                    lines.stream().filter(s -> !s.startsWith("//") && !s.isEmpty()).forEach(list::add);
                    MAPPINGS.put(dep, list);
                }
            } else {
                System.out.println("Found Headless file, can't read! " + file.toString());
            }
        } else {
            System.out.println("Found empty file: " + file.toString());
        }
    }

    public static void processMappings(List<ClassMapping> mappings){
        mappings.stream().filter(classMapping -> classMapping.getMappedName() != null).forEach(classMapping -> RESOLVED_DEPENDENCIES.add(classMapping.getMappedName().replace("/", ".")));
        AtomicInteger processed = new AtomicInteger();
        do {
            processed.set(0);
            MAPPINGS.entrySet().stream().filter(stringListEntry -> RESOLVED_DEPENDENCIES.contains(stringListEntry.getKey())).forEach(stringListEntry -> {
                run(stringListEntry.getValue());
                processed.getAndIncrement();
                MAPPINGS.remove(stringListEntry.getKey());
            });
        } while (processed.get() > 0);
    }

    private static void run(List<String> lines){
        for(String line : lines){
            if(line.contains(".")){
                runLine(line);
            }
        }
    }

    private static void runLine(String line){
        String[] split = line.split("\\.", 2);
        if(split.length > 0){
            if(!split[0].contains("(") && split[1].contains("(")){
                Class<?> owner = Scripts.findClass(split[0]);

                String[] task = split[1].split("\\(", 2);
                String taskName = task[0];
                List<String> taskArgs = isolateArgs(task[1]);
                List<Object> args = new ArrayList<>();
                for(String taskArg : taskArgs){
                    if(!taskArg.startsWith("\"")){
                        runLine(taskArg);
                    } else {

                    }
                }

                System.out.println(owner + "   " + Arrays.toString(split));
            } else {
                System.out.println("First call can't a function call! " + line);
            }
        }
    }

    private static List<String> isolateArgs(String line){
        List<String> s = new ArrayList<>();
        int open = 0;
        char[] charArray = line.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            Character c = charArray[i];
            if (c.equals('(')) {
                open++;
            } else if (c.equals(')')) {
                if (open == 0) {
                    Collections.addAll(s, line.substring(0, i).split(","));
                    open = 0;
                } else {
                    open--;
                }
            }
        }
        return s;
    }

    public static void clean(){
        MAPPINGS.clear();
        RESOLVED_DEPENDENCIES.clear();
    }

}
