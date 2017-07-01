package de.canitzp.libloader.remap;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.remap.mappings.MappingsBase;
import de.canitzp.libloader.remap.mappings.MappingsDependsOn;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author canitzp
 */
public class Mappings {

    public static List<ClassMapping> classMappings;

    public static void findClassNames() throws InvocationTargetException, IllegalAccessException {
        List<String> finishedMappings = new ArrayList<>();
        Map<String, List<Method>> dependencyMethods = new ConcurrentHashMap<>();
        for(Class<? extends MappingsBase> mappingsClass : LibLoader.MAPPINGS_CLASSES){
            Method[] methods = mappingsClass.getDeclaredMethods();
            for(Method method : methods){
                if(method.isAnnotationPresent(MappingsDependsOn.class)){
                    MappingsDependsOn deps = method.getAnnotation(MappingsDependsOn.class);
                    if(!finishedMappings.contains(deps.value())){
                        List<Method> methodList = dependencyMethods.containsKey(deps.value()) ? dependencyMethods.get(deps.value()) : new ArrayList<>();
                        methodList.add(method);
                        dependencyMethods.put(deps.value(), methodList);
                        continue;
                    }
                }
                method.invoke(null);
                finishedMappings.add(method.getName());
                invokeAndLook(method.getName(), dependencyMethods, finishedMappings);
            }
        }
    }

    private static void invokeAndLook(String methodName, Map<String, List<Method>> dependencyMethods, List<String> finishedMappings) throws InvocationTargetException, IllegalAccessException {
        if(dependencyMethods.containsKey(methodName)){
            for(Method method1 : dependencyMethods.get(methodName)){
                method1.invoke(null);
                finishedMappings.add(method1.getName());
                invokeAndLook(method1.getName(), dependencyMethods, finishedMappings);
            }
        }
    }

    @Nullable
    public static ClassMapping getClassMappingFromObfName(@NotNull String obfName){
        for(ClassMapping classMapping : classMappings){
            if(classMapping.getObfName().equals(obfName)){
                return classMapping;
            }
        }
        return null;
    }

    @Nullable
    public static ClassMapping getClassMappingFromMappedName(@NotNull String mappedName){
        for(ClassMapping classMapping : classMappings){
            if(classMapping.getMappedName() != null && classMapping.getMappedName().equals(mappedName)){
                return classMapping;
            }
        }
        return null;
    }

    @NotNull
    public static List<ChildMapping<MethodNode>> getMethodFromObf(@Nullable ClassMapping classMapping, @Nullable String obfName, @Nullable String obfDesc){
        if(classMapping != null){
            List<ChildMapping<MethodNode>> ret = new ArrayList<>();
            for(ChildMapping<MethodNode> method : classMapping.getMethods()){
                if(obfName != null && !method.getObfuscatedName().equals(obfName)){
                    continue;
                }
                if(obfDesc != null && !method.getObfuscatedDesc().equals(obfDesc)){
                    continue;
                }
                ret.add(method);
            }
            return ret;
        }
        return Collections.emptyList();
    }

    @Nullable
    public static ChildMapping<MethodNode> getMethodFromMapped(@Nullable ClassMapping classMapping, @NotNull String mappedName, @NotNull String mappedDesc){
        if(classMapping != null){
            List<ChildMapping<MethodNode>> ret = new ArrayList<>();
            for(ChildMapping<MethodNode> method : classMapping.getMethods()){
                if(method.getMappedName().equals(mappedName) && method.getMappedDesc().equals(mappedDesc)){
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean addClassMapping(@NotNull String obfName, @NotNull String mappedName){
        ClassMapping classMapping = getClassMappingFromObfName(obfName);
        if(classMapping != null){
            if(classMapping.getMappedName() != null){
                LibLog.msg(Level.WARNING, String.format("Your overriding a mapped name for a class! From '%s' to '%s'", classMapping.getMappedName(), mappedName));
            }
            classMapping.setMappedName(mappedName);
            return true;
        }
        return false;
    }

    public static boolean addMethodMapping(@NotNull ClassMapping classMapping, @NotNull ChildMapping<MethodNode> method, @NotNull String mappedName, @NotNull String mappedDesc){
        for(ChildMapping<MethodNode> method1 : classMapping.getMethods()){
            if(method1 == method){
                method1.setMapped(mappedName, mappedDesc);
                return true;
            }
        }
        return false;
    }

    public static boolean addMappingsWithMethodInsnNode(@NotNull MethodInsnNode node, @NotNull String mappedClassName, @NotNull String mappedMethodName, @NotNull String mappedMethodDesc){
        if(addClassMapping(node.owner, mappedClassName)){
            ClassMapping classMapping = getClassMappingFromMappedName(mappedClassName);
            List<ChildMapping<MethodNode>> methods = getMethodFromObf(classMapping, node.name, node.desc);
            if(methods.size() == 1){
                return addMethodMapping(classMapping, methods.get(0), mappedMethodName, mappedMethodDesc);
            }
        }
        return false;
    }

    public static AbstractInsnNode getOpcodeSequence(InsnList list, int... opcodes) {
        boolean flag = false;
        AbstractInsnNode lastNode = list.getFirst();
        while (lastNode != null){
            for(int code : opcodes){
                lastNode = lastNode.getNext();
                if(lastNode.getOpcode() != code){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return lastNode;
            } else {
                flag = false;
            }
        }
        return null;
    }

}
