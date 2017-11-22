package de.canitzp.libloader.remap;

import de.canitzp.libloader.launch.NameTransformer;
import org.apache.commons.lang3.tuple.Triple;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class CustomRemapper extends Remapper {

    public List<ClassMapping> mappings;
    private Map<String, ClassMapping> cachedClassMappings = new HashMap<>();

    public CustomRemapper(List<ClassMapping> mappings){
        this.mappings = mappings;
    }

    public ClassMapping getClassMappingForName(String name){
        if(name != null){
            name = name.replace("/", ".").replace(".", "/");
            if(cachedClassMappings.containsKey(name)){
                return this.cachedClassMappings.get(name);
            }
            for(ClassMapping mapping : mappings){
                if(name.equals(mapping.getObfName()) || name.equals(mapping.getMappedName())){
                    cachedClassMappings.put(name, mapping);
                    return mapping;
                }
            }
        }
        return null;
    }

    // return with dots
    public String remapClassName(String name, boolean force){
        String newName = name.replace(".", "/");
        if(!newName.contains("$") || force){
            ClassMapping mapping = this.getClassMappingForName(newName);
            if(mapping != null && mapping.getMappedName() != null){
                return mapping.getMappedName().replace("/", ".");
            }
        } else {
            String[] split = newName.split("\\$");
            newName = remapClassName(split[0], true);
            for(int i = 1; i < split.length; i++){
                newName = remapClassName(newName + "$" + split[i], true);
            }
            return newName.replace("/", ".");
        }
        return name;
    }

    // return with dots
    public String unmapClassName(String name, boolean force){
        String newName = name.replace(".", "/");
        if(!newName.contains("$") || force){
            ClassMapping mapping = this.getClassMappingForName(newName);
            if(mapping != null){
                return mapping.getObfName().replace("/", ".");
            }
        } else {
            String[] split = newName.split("\\$");
            newName = unmapClassName(split[0], true);
            for(int i = 1; i < split.length; i++){
                newName = unmapClassName(newName + "$" + split[i], true);
            }
            return newName.replace("/", ".");
        }
        return name;
    }

    private Map<String, String> cachedClassNames = new HashMap<>();
    private Map<Triple<String, String, String>, String> cachedMethodNames = new HashMap<>();
    private Map<Triple<String, String, String>, String> cachedFieldNames = new HashMap<>();
    private Map<Triple<String, String, String>, Integer> cachedMethodAccess = new HashMap<>();
    private Map<Triple<String, String, String>, Integer> cachedFieldAccess = new HashMap<>();

    @Override // return with /
    public String map(String typeName) {
        if(cachedClassNames.containsKey(typeName)){
            return cachedClassNames.get(typeName);
        }
        String newTypeName = typeName;
        if(NameTransformer.isMinecraftClass(typeName)){
            newTypeName = this.remapClassName(typeName, false).replace(".", "/");
        }
        cachedClassNames.put(typeName, newTypeName);
        return newTypeName;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        for(Map.Entry<Triple<String, String, String>, String> entry : cachedMethodNames.entrySet()){
            if(entry.getKey().equals(Triple.of(owner, name, desc))){
                return entry.getValue();
            }
        }
        String newMethodName = name;
        if(NameTransformer.isMinecraftClass(owner)){
            String className = this.map(owner);
            ClassMapping mapping = this.getClassMappingForName(className);
            if(mapping != null){
                for(ChildMapping<MethodNode> method : mapping.getMethods()){
                    if(name.equals(method.getObfuscatedName()) && desc.equals(method.getObfuscatedDesc()) && method.getMappedName() != null){
                        newMethodName = method.getMappedName();
                        break;
                    }
                }
            }
        }
        cachedMethodNames.put(Triple.of(owner, name, desc), newMethodName);
        return newMethodName;
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        for(Map.Entry<Triple<String, String, String>, String> entry : cachedFieldNames.entrySet()){
            if(entry.getKey().equals(Triple.of(owner, name, desc))){
                return entry.getValue();
            }
        }
        String newFieldName = name;
        if(NameTransformer.isMinecraftClass(owner)){
            String className = this.map(owner);
            ClassMapping mapping = this.getClassMappingForName(className);
            if(mapping != null){
                for(ChildMapping<FieldNode> field : mapping.getFields()){
                    if(name.equals(field.getObfuscatedName()) && desc.equals(field.getObfuscatedDesc()) && field.getMappedName() != null){
                        newFieldName = field.getMappedName();
                        break;
                    }
                }
            }
        }
        cachedFieldNames.put(Triple.of(owner, name, desc), newFieldName);
        return newFieldName;
    }

    public String mapInnerClassName(String name, String outerName, String innerName){
        //TODO maybe fernflower does my work
        return innerName;
    }

    public int mapFieldAccess(int access, String owner, String name, String desc){
        for(Map.Entry<Triple<String, String, String>, Integer> entry : cachedFieldAccess.entrySet()){
            if(entry.getKey().equals(Triple.of(owner, name, desc))){
                return entry.getValue();
            }
        }
        int newAccess = access;
        if(NameTransformer.isMinecraftClass(owner)){
            String className = this.map(owner);
            ClassMapping mapping = this.getClassMappingForName(className);
            if(mapping != null){
                for(ChildMapping<FieldNode> field : mapping.getFields()){
                    if(name.equals(field.getObfuscatedName()) && desc.equals(field.getObfuscatedDesc()) && field.getAccess() > -1){
                        newAccess = field.getAccess();
                        System.out.println("new access " + newAccess);
                        break;
                    }
                }
            }
        }
        cachedFieldAccess.put(Triple.of(owner, name, desc), newAccess);
        return newAccess;
    }

    public int mapMethodAccess(int access, String owner, String name, String desc){
        for(Map.Entry<Triple<String, String, String>, Integer> entry : cachedMethodAccess.entrySet()){
            if(entry.getKey().equals(Triple.of(owner, name, desc))){
                return entry.getValue();
            }
        }
        int newAccess = access;
        if(NameTransformer.isMinecraftClass(owner)){
            String className = this.map(owner);
            ClassMapping mapping = this.getClassMappingForName(className);
            if(mapping != null){
                for(ChildMapping<MethodNode> method : mapping.getMethods()){
                    if(name.equals(method.getObfuscatedName()) && desc.equals(method.getObfuscatedDesc()) && method.getAccess() > -1){
                        newAccess = method.getAccess();
                        break;
                    }
                }
            }
        }
        cachedMethodAccess.put(Triple.of(owner, name, desc), newAccess);
        return newAccess;
    }

}
