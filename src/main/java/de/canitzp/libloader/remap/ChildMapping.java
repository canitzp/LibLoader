package de.canitzp.libloader.remap;

/**
 * @author canitzp
 */
public class ChildMapping<T> {

    private JavaDocMapping javaDoc;

    private String obfuscatedName, obfuscatedDesc, mappedName, mappedDesc;
    private T node;

    public ChildMapping setObfName(String obfName, String obfDesc){
        this.obfuscatedName = obfName;
        this.obfuscatedDesc = obfDesc;
        return this;
    }

    public ChildMapping setMapped(String mappedName, String mappedDesc){
        this.mappedName = mappedName;
        this.mappedDesc = mappedDesc;
        return this;
    }

    public ChildMapping setJavaDoc(JavaDocMapping javaDoc){
        this.javaDoc = javaDoc;
        return this;
    }

    public ChildMapping setNode(T node){
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

    public T getNode() {
        return node;
    }
}
