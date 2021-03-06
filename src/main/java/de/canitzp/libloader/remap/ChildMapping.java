package de.canitzp.libloader.remap;


import de.canitzp.libloader.Util;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author canitzp
 */
public class ChildMapping<T> {

    private JavaDocMapping javaDoc;
    private ClassMapping parent;

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

    public ChildMapping<T> setParent(ClassMapping parent){
        this.parent = parent;
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

    public ClassMapping getParent(){
        return this.parent;
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

    public List<String> getSpecialProperties(boolean isCompareOutput){
        List<String> lines = new ArrayList<>();
        if(isCompareOutput){
            if(getNode() instanceof MethodNode){
                String line = "";
                List<ParameterNode> params = ((MethodNode) getNode()).parameters;
                if(params != null){
                    line += "parameters=" + Arrays.deepToString(params.toArray());
                }
                if(!StringUtils.isEmpty(line)){
                    lines.add(":MInfo " + line);
                }
            } else if(getNode() instanceof FieldNode){

            }
        }
        return lines;
    }

    public float getMatchProbability(int access, String desc, int internalUsages, int outerUsages){
        float chance = 1.0F;
        access = Util.trimAccess(access);
        if(getNode() instanceof FieldNode){
            FieldNode field = (FieldNode) getNode();
            int testAccess = Util.trimAccess(field.access);
            if((testAccess & Opcodes.ACC_STATIC) != (access & Opcodes.ACC_STATIC)){
                chance -= 0.05F;
            }
            if((testAccess & Opcodes.ACC_FINAL) != (access & Opcodes.ACC_FINAL)){
                chance -= 0.05F;
            }
            if(!field.desc.equals(desc)){
                chance -= 0.2F;
            }
            if(internalUsages != -1){
                chance -= Math.abs(internalUsages - this.getParent().getFieldUsages(field)) / 100.0F;
            }
            if(outerUsages != -1 && chance >= 0.95F && !Modifier.isPrivate(field.access)){
                for(ClassMapping cm : Mappings.classMappings){
                    if(cm != this.getParent()){
                        chance -= Math.abs(outerUsages - cm.getFieldUsages(field));
                    }
                }
            }
        }
        return chance;
    }
}
