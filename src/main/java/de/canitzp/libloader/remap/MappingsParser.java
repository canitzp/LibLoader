package de.canitzp.libloader.remap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class MappingsParser {

    public static final String UNKNOWN_PREFIX = "net/minecraft/class_";
    public static boolean compareMode = false;
    public static String nameOverride = null;

    private File mappingsFile;

    public MappingsParser(File mappingsFile){
        this.mappingsFile = mappingsFile;
        if(nameOverride != null){
            this.mappingsFile = new File(this.mappingsFile.getParent(), nameOverride);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ClassMapping> read() throws IOException {
        List<ClassMapping> mapperList = new ArrayList<>();
        ClassMapping currentMapping = null;
        ChildMapping currentChild = null;
        for(String line : FileUtils.readLines(this.mappingsFile, "UTF-8")){
            if(!line.startsWith(":")){
                if(currentMapping != null){
                    mapperList.add(currentMapping);
                }
                currentMapping = readClassMappingFromLine(line);
            } else if(currentMapping != null){
                if(line.startsWith(":M ")){
                    currentMapping.addMethod(currentChild = this.readChildMappingFromLine(line.substring(3)));
                } else if(line.startsWith(":F ")){
                    currentMapping.addField(currentChild = this.readChildMappingFromLine(line.substring(3)));
                } else if(line.startsWith(":JD ")){
                    if(currentChild != null){
                        currentChild.setJavaDoc(readJavaDocMappingFromLine(line.substring(4)));
                    } else {
                        currentMapping.setJavaDoc(readJavaDocMappingFromLine(line.substring(4)));
                    }
                } else if(line.startsWith(":AT ")){
                    if(currentChild != null){
                        currentChild.setAccess(this.readAccessTransformerFromLine(line.substring(4)));
                    }
                } else if(line.startsWith(":MP ")){
                    if(currentChild != null){
                        currentChild.setParamNames(this.readParamNamesFromFile(line.substring(4)));
                    }
                }
            } else {
                System.out.println("Your Mappings file is corrupted!");
                return Collections.emptyList();
            }
        }
        if(currentMapping != null){
            mapperList.add(currentMapping);
        }
        return mapperList;
    }

    public void write(List<ClassMapping> classMappings) throws IOException {
        List<String> lines = new ArrayList<>();
        for(ClassMapping mapping : classMappings){
            lines.add(mapping.getObfName() + "#" + emptyIfNull(mapping.getMappedName()));
            if(mapping.getJavaDoc() != null){
                lines.add(":JD " + mapping.getJavaDoc().getRawLines());
            }
            for(ChildMapping<MethodNode> method : mapping.getMethods()){
                lines.add(":M " + method.getObfuscatedName() + "#" + method.getObfuscatedDesc() + "#" + emptyIfNull(method.getMappedName()) + "#" + emptyIfNull(method.getMappedDesc()));
                lines.addAll(method.getSpecialProperties(compareMode));
                if(method.getJavaDoc() != null){
                    lines.add(":JD " + method.getJavaDoc().getRawLines());
                }
                if(method.getAccess() > -1){
                    lines.add(":AT " + String.valueOf(method.getAccess()));
                }
                if(method.getParamNames() != null){
                    lines.add(":MP " + String.join(",", method.getParamNames()));
                }
            }
            for(ChildMapping<FieldNode> field : mapping.getFields()){
                lines.add(":F " + field.getObfuscatedName() + "#" + field.getObfuscatedDesc() + "#" + emptyIfNull(field.getMappedName()) + "#" + emptyIfNull(field.getMappedDesc()));
                if(field.getJavaDoc() != null){
                    lines.add(":JD " + field.getJavaDoc().getRawLines());
                }
                if(field.getAccess() > -1){
                    lines.add(":AT " + String.valueOf(field.getAccess()));
                }
            }
        }
        FileUtils.writeLines(this.mappingsFile, "UTF-8", lines);
    }

    private String emptyIfNull(String s){
        return s != null ? s : "";
    }

    private ClassMapping readClassMappingFromLine(String line){
        ClassMapping mapping = new ClassMapping();
        String[] lines = line.split("#");
        mapping.setObfName(lines[0]);
        if(lines.length == 2){
            mapping.setMappedName(lines[1]);
        } else {
            mapping.setMappedName(UNKNOWN_PREFIX + lines[0]);
        }
        return mapping;
    }

    private <T> ChildMapping<T> readChildMappingFromLine(String line){
        ChildMapping<T> childMapping = new ChildMapping<>();
        String[] lines = line.split("#");
        childMapping.setObfName(lines[0], lines[1]);
        if(lines.length == 4){
            childMapping.setMapped(lines[2], lines[3]);
        }
        return childMapping;
    }

    private JavaDocMapping readJavaDocMappingFromLine(String line){
        JavaDocMapping javaDoc = new JavaDocMapping();
        for(String s : line.split(JavaDocMapping.LINE_SPLIT_PATTERN.pattern())){
            javaDoc.addLine(s);
        }
        return javaDoc;
    }

    private int readAccessTransformerFromLine(String line){
        if(NumberUtils.isCreatable(line.replace(" ", ""))){
            return Integer.parseInt(line.replace(" ", ""));
        }
        return -1;
    }

    private String[] readParamNamesFromFile(String line){
        String[] split = line.split(",");
        if(split.length > 0){
            return split;
        }
        return null;
    }

}
