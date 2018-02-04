package de.canitzp.libloader.remap;

import de.canitzp.libloader.launch.ITransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author canitzp
 */
public class ClassMapping {

    private String obfName, mappedName;
    private List<ChildMapping<MethodNode>> methods = new ArrayList<>();
    private List<ChildMapping<FieldNode>> fields = new ArrayList<>();
    private JavaDocMapping javaDoc;
    private ClassReader classReader;
    private ClassNode cachedClassNode;
    private List<ITransformer> transformer = new ArrayList<>();

    private final Map<FieldNode, Integer> CACHED_USAGES = new ConcurrentHashMap<>();

    public ClassMapping setObfName(String obfName){
        this.obfName = obfName;
        return this;
    }

    public ClassMapping setMappedName(String mappedName){
        this.mappedName = mappedName;
        return this;
    }

    public ClassMapping addMethod(ChildMapping<MethodNode> method){
        this.methods.add(method);
        return this;
    }

    public ClassMapping addField(ChildMapping<FieldNode> field){
        this.fields.add(field);
        return this;
    }

    public ClassMapping setJavaDoc(JavaDocMapping javaDoc){
        this.javaDoc = javaDoc;
        return this;
    }

    public ClassMapping setClassReader(ClassReader classReader){
        this.classReader = classReader;
        return this;
    }

    public ClassMapping setClassNode(ClassNode classNode){
        this.cachedClassNode = classNode;
        return this;
    }

    public ClassMapping addClassTransformer(ITransformer transformer){
        this.transformer.add(transformer);
        return this;
    }

    public String getObfName() {
        return obfName;
    }

    public String getMappedName() {
        return mappedName;
    }

    public boolean hasMappedName(){
        return mappedName != null && !Objects.equals("", mappedName);
    }

    public List<ChildMapping<MethodNode>> getMethods() {
        return methods;
    }

    public List<ChildMapping<FieldNode>> getFields() {
        return fields;
    }

    public JavaDocMapping getJavaDoc() {
        return javaDoc;
    }

    public ClassReader getClassReader() {
        return classReader;
    }

    public ClassNode getClassNode(){
        if(this.cachedClassNode != null){
            return this.cachedClassNode;
        } else if(this.getClassReader() != null){
            ClassNode cn = new ClassNode();
            this.getClassReader().accept(cn, 0);
            return this.cachedClassNode = cn;
        }
        return null;
    }

    public ClassNode getClassNodeWithMethodAndFields(){
        ClassNode cn = this.getClassNode();
        cn.methods = new ArrayList<>();
        for(ChildMapping<MethodNode> method : this.getMethods()){
            cn.methods.add(method.getNode());
        }
        cn.fields = new ArrayList<>();
        for(ChildMapping<FieldNode> field : this.getFields()){
            cn.fields.add(field.getNode());
        }
        return cn;
    }

    @Deprecated
    public ChildMapping<MethodNode> getMethodByName(String name){
        for(ChildMapping<MethodNode> method : this.getMethods()){
            if(name.equals(method.getObfuscatedName()) || name.equals(method.getMappedName())){
                return method;
            }
        }
        return null;
    }

    public ChildMapping<FieldNode> getFieldByName(String name){
        for(ChildMapping<FieldNode> field : this.getFields()){
            if(name.equals(field.getObfuscatedName()) || name.equals(field.getMappedName())){
                return field;
            }
        }
        return null;
    }

    public ChildMapping<MethodNode> getMethodByNameAndDesc(String name, String desc){
        for(ChildMapping<MethodNode> method : this.getMethods()){
            if((name.equals(method.getObfuscatedName()) || name.equals(method.getMappedName())) && (desc.equals(method.getObfuscatedDesc()) || desc.equals(method.getMappedDesc()))){
                return method;
            }
        }
        return null;
    }

    public byte[] getRawData(){
        ClassNode cn = this.getClassNode();
        for (ITransformer transformer : this.transformer) {
            transformer.transformClass(cn);
            for(MethodNode mn : cn.methods) {
                transformer.transformMethod(cn, mn);
            }
            for(FieldNode fn : cn.fields){
                transformer.transformField(cn, fn);
            }
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | Opcodes.ASM5){
            /*@Override
            protected String getCommonSuperClass(String type1, String type2) {
                Class<?> c, d;
                LaunchClassLoader classLoader = Launch.classLoader;
                try {
                    //c = Class.forName(type1.replace('/', '.'), false, classLoader);
                    //d = Class.forName(type2.replace('/', '.'), false, classLoader);
                    c = classLoader.findClass(type1.replace('/', '.'));
                    d = classLoader.findClass(type2.replace('/', '.'));
                } catch (Exception e) {
                    try {
                        c = classLoader.findClass(NameTransformer.remapper.unmapClassName(type1.replace('/', '.'), false));
                        d = classLoader.findClass(NameTransformer.remapper.unmapClassName(type2.replace('/', '.'), false));
                    } catch (Exception e1){
                        throw new RuntimeException(e.toString() + " & " + e1.toString());
                    }
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }*/
        };
        cn.accept(cw);
        return cw.toByteArray();
    }

    public int getFieldUsages(FieldNode field){
        int count = CACHED_USAGES.getOrDefault(field, -1);
        if(count == -1){
            count = 0;
            for(ChildMapping<MethodNode> methods : this.getMethods()){
                count += Arrays.stream(methods.getNode().instructions.toArray())
                        .filter(abstractInsnNode -> abstractInsnNode instanceof FieldInsnNode)
                        .filter(abstractInsnNode -> ((FieldInsnNode) abstractInsnNode).desc.equals(field.desc) && ((FieldInsnNode) abstractInsnNode).name.equals(field.name)).count();
            }
            CACHED_USAGES.put(field, count);
        }
        return count;
    }

    public void init(){
        if(this.getObfName().contains("/")){
            this.setMappedName(this.getObfName());
        }
        ClassNode cn = this.getClassNode();
        for(MethodNode method : cn.methods){
            ChildMapping<MethodNode> child = new ChildMapping<MethodNode>().setObfName(method.name, method.desc).setNode(method).setParent(this);
            if(method.name.equals("<init>")){
                child.setMapped(method.name, null);
            } else if(method.name.equals("<clinit>")){
                child.setMapped(method.name, method.desc);
            }
            this.addMethod(child);
        }
        for(FieldNode field : cn.fields){
            this.addField(new ChildMapping<FieldNode>().setObfName(field.name, field.desc).setNode(field).setParent(this));
        }
    }

    @Override
    public String toString() {
        return String.format("ClassMapping{obfName=%s, mappedName=%s, hasClassReader=%b, javaDoc=%s, methods=%s, fields=%s}", this.obfName, this.mappedName, this.classReader != null, this.javaDoc, this.methods, this.fields);
    }
}
