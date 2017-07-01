package de.canitzp.libloader.remap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ClassMapping {

    private String obfName, mappedName;
    private List<ChildMapping<MethodNode>> methods = new ArrayList<>();
    private List<ChildMapping<FieldNode>> fields = new ArrayList<>();
    private JavaDocMapping javaDoc;
    private ClassReader classReader;

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

    public String getObfName() {
        return obfName;
    }

    public String getMappedName() {
        return mappedName;
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

    public ClassNode createClassNode(){
        if(this.getClassReader() != null){
            ClassNode cn = new ClassNode();
            this.getClassReader().accept(cn, 0);
            return cn;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("ClassMapping{obfName=%s, mappedName=%s, hasClassReader=%b, javaDoc=%s, methods=%s, fields=%s}", this.obfName, this.mappedName, this.classReader != null, this.javaDoc, this.methods, this.fields);
    }
}
