package de.canitzp.libloader.remap;

import de.canitzp.libloader.LibLoader;
import de.canitzp.libloader.LibLog;
import de.canitzp.libloader.Names;
import de.canitzp.libloader.remap.mappings.MappingsBase;
import de.canitzp.libloader.remap.mappings.MappingsDependsOn;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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

    public static void throwRemappingError(String message){
        LibLog.msg(Level.ALL, message, true);
    }

    public static ClassMapping getClassMappingFromObfName(String obfName){
        for(ClassMapping classMapping : classMappings){
            if(classMapping.getObfName().equals(obfName)){
                return classMapping;
            }
        }
        return null;
    }

    public static ClassMapping getClassMappingFromMappedName(Names name){
            return getClassMappingFromMappedName(name.getClassDefName());
    }

    public static ClassMapping getClassMappingFromMappedName(String mappedName){
        for(ClassMapping classMapping : classMappings){
            if(classMapping.getMappedName() != null && classMapping.getMappedName().equals(mappedName)){
                return classMapping;
            }
        }
        return null;
    }

    public static ClassMapping getClassMappingFromName(String name){
        for(ClassMapping classMapping : classMappings){
            if(name.equals(classMapping.getObfName()) || name.equals(classMapping.getMappedName())){
                return classMapping;
            }
        }
        return null;
    }

    public static List<ClassMapping> findClassWithString(String... ss){
        List<ClassMapping> possibleClasses = new ArrayList<>();
        for(ClassMapping classMapping : classMappings){
            List<String> foundInClass = new ArrayList<>();
            for(ChildMapping<MethodNode> method : classMapping.getMethods()){
                foundInClass.addAll(getStrings(method.getNode().instructions));
            }
            if(foundInClass.containsAll(Arrays.asList(ss))){
                possibleClasses.add(classMapping);
            }
        }
        return possibleClasses;
    }

    public static List<ChildMapping<MethodNode>> getMethodFromObf(ClassMapping classMapping, String obfName, String obfDesc){
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

    public static ChildMapping<MethodNode> getMethodFromMapped(ClassMapping classMapping, String mappedName, String mappedDesc){
        if(classMapping != null){
            List<ChildMapping<MethodNode>> ret = new ArrayList<>();
            for(ChildMapping<MethodNode> method : classMapping.getMethods()){
                if(mappedName.equals(method.getMappedName()) && mappedDesc.equals(method.getMappedDesc())){
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean addClassMapping(String obfName, String mappedName){
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

    public static boolean addMethodMapping(ClassMapping classMapping, ChildMapping<MethodNode> method, String mappedName, String mappedDesc){
        for(ChildMapping<MethodNode> method1 : classMapping.getMethods()){
            if(method1 == method){
                method1.setMapped(mappedName, mappedDesc);
                return true;
            }
        }
        return false;
    }

    public static boolean addFieldMapping(ClassMapping classMapping, ChildMapping<FieldNode> field, String mappedName, String mappedDesc){
        for(ChildMapping<FieldNode> field1 : classMapping.getFields()){
            if(field1 == field){
                field1.setMapped(mappedName, mappedDesc);
                return true;
            }
        }
        return false;
    }

    public static boolean addMappingsWithMethodInsnNode(MethodInsnNode node, String mappedClassName, String mappedMethodName, String mappedMethodDesc){
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

    public static List<LdcInsnNode> getLdcNodes(InsnList list){
        List<LdcInsnNode> ret = new ArrayList<>();
       for(AbstractInsnNode node : list.toArray()){
           if(node instanceof LdcInsnNode){
               ret.add((LdcInsnNode) node);
           }
       }
       return ret;
    }

    public static List<String> getStrings(InsnList list){
        List<String> ret = new ArrayList<>();
        for(LdcInsnNode node : getLdcNodes(list)){
            if(node.cst instanceof String){
                ret.add((String) node.cst);
            }
        }
        return ret;
    }

    public static List<ChildMapping<MethodNode>> getMethodByStrings(ClassMapping cm, String... ss){
        List<ChildMapping<MethodNode>> ret = new ArrayList<>();
        for(ChildMapping<MethodNode> method : cm.getMethods()){
            List<String> stringInMethod = getStrings(method.getNode().instructions);
            boolean errorFlag = false;
            for(String s : ss){
                if(!stringInMethod.contains(s)){
                    if(errorFlag){
                        break;
                    } else {
                        errorFlag = true;
                    }
                }
            }
            if(!errorFlag){
                ret.add(method);
            }
        }
        return ret;
    }

    public static MethodInsnNode findInvokeNode(InsnList list, String owner, String name, String desc){
        for(AbstractInsnNode node : list.toArray()){
            if(node.getOpcode() == Opcodes.INVOKEVIRTUAL){
                MethodInsnNode insnNode = (MethodInsnNode) node;
                if(owner.equals(insnNode.owner) && name.equals(insnNode.name) && desc.equals(insnNode.desc)){
                    return insnNode;
                }
            }
        }
        return null;
    }

}
