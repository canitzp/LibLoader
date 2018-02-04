package de.canitzp.mappings;

import de.canitzp.libloader.remap.ClassMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author canitzp
 */
public class ClassPool {

    private static final Map<String, ClassMapping> MAPPED = new ConcurrentHashMap<>();
    private static final Map<String, ClassMapping> UNMAPPED = new ConcurrentHashMap<>();

    public static void print(){
        MAPPED.values().forEach(System.out::println);
    }

    public static ClassWrapper get(String name){
        return new ClassWrapper(MAPPED.get(name));
    }

    public static ClassWrapper raw(String name){
        return new ClassWrapper(UNMAPPED.get(name));
    }

    public static ClassMapping add(ClassMapping mapping){
        if(mapping.getMappedName() != null){
            MAPPED.put(mapping.getMappedName().replace("/", "."), mapping);
            if(UNMAPPED.containsKey(mapping.getObfName())){
                UNMAPPED.remove(mapping.getObfName());
            }
        } else {
            UNMAPPED.put(mapping.getObfName().replace("/", "."), mapping);
        }
        return mapping;
    }

    public static ClassWrapper add(ClassWrapper mapping){
        return mapping.getNode() != null ? new ClassWrapper(add(mapping.getNode())) : null;
    }

    public static MethodWrapper getMethod(ClassWrapper classWrapper, String methodName, String methodDesc){
        return new MethodWrapper(classWrapper.getNode() != null ? classWrapper.getNode().getMethodByNameAndDesc(methodName, methodDesc) : null);
    }

    public static MethodWrapper getMethod(ClassWrapper classWrapper, String methodName){
        return new MethodWrapper(classWrapper.getNode() != null ? classWrapper.getNode().getMethodByName(methodName) : null);
    }

}
