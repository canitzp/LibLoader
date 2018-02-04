package de.canitzp.mappings;

import de.canitzp.libloader.remap.ClassMapping;

/**
 * @author canitzp
 */
public class ClassWrapper {

    private ClassMapping node;

    public ClassWrapper(ClassMapping node) {
        this.node = node;
    }

    public ClassMapping getNode() {
        return node;
    }

    public ClassWrapper setMapping(String name){
        if(this.node != null){
            this.node.setMappedName(name);
        }
        return this;
    }
}
