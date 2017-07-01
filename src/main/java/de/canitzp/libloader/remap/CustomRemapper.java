package de.canitzp.libloader.remap;

import org.objectweb.asm.commons.Remapper;

import java.util.List;

/**
 * @author canitzp
 */
public class CustomRemapper extends Remapper {

    private List<ClassMapping> mappings;

    public CustomRemapper(List<ClassMapping> mappings){
        this.mappings = mappings;
    }

    @Override
    public String map(String typeName) {
        if(!typeName.contains("/") || typeName.startsWith("net/minecraft/")){
            for(ClassMapping classMapping : mappings){
                if(classMapping.getObfName().equals(typeName) && classMapping.getMappedName() != null){
                    return classMapping.getMappedName();
                }
            }
        }
        return super.map(typeName);
    }
}
