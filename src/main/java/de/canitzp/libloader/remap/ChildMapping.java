package de.canitzp.libloader.remap;


import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author canitzp
 */
public class ChildMapping<T> {

    private JavaDocMapping javaDoc;

    private String obfuscatedName, obfuscatedDesc, mappedName, mappedDesc;
    private int access = -1; // -1 is default and means that nothing does change
    private String[] paramNames;
    private T node;

    public ChildMapping(){}

    public ChildMapping(MethodNode method){
        this.setObfName(method.name, method.desc);
        this.setNode((T) method);
    }

    public ChildMapping(FieldNode field){
        this.setObfName(field.name, field.desc);
        this.setNode((T) field);
    }

    public ChildMapping<T> setObfName(String obfName, String obfDesc){
        this.obfuscatedName = obfName;
        this.obfuscatedDesc = obfDesc;
        return this;
    }

    public ChildMapping<T> setMapped(String mappedName, String mappedDesc){
        this.mappedName = mappedName;
        this.mappedDesc = mappedDesc;
        return this;
    }

    public ChildMapping<T> setJavaDoc(JavaDocMapping javaDoc){
        this.javaDoc = javaDoc;
        return this;
    }

    public ChildMapping<T> setAccess(int access){
        this.access = access;
        return this;
    }

    public ChildMapping<T> setParamNames(String... names){
        this.paramNames = names;
        return this;
    }

    public ChildMapping<T> setNode(T node){
        this.node = node;
        return this;
    }

    public JavaDocMapping getJavaDoc() {
        return javaDoc;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getObfuscatedDesc() {
        return obfuscatedDesc;
    }

    public String getMappedName() {
        return mappedName;
    }

    public String getMappedDesc() {
        return mappedDesc;
    }

    public int getAccess() {
        return access;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public T getNode() {
        return node;
    }

    public boolean isSameAs(T instance){
        if(instance instanceof MethodNode){
            MethodNode m = (MethodNode) instance;
            return (m.name.equals(this.getObfuscatedName()) || m.name.equals(this.getMappedName())) && (m.desc.equals(this.getObfuscatedDesc()) || m.desc.equals(this.getMappedDesc()));
        } else if(instance instanceof FieldNode){
            FieldNode m = (FieldNode) instance;
            return (m.name.equals(this.getObfuscatedName()) || m.name.equals(this.getMappedName())) && (m.desc.equals(this.getObfuscatedDesc()) || m.desc.equals(this.getMappedDesc()));
        }
        return false;
    }
}
