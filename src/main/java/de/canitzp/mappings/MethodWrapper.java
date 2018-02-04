package de.canitzp.mappings;

import de.canitzp.libloader.remap.ChildMapping;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author canitzp
 */
public class MethodWrapper {

    private ChildMapping<MethodNode> node;

    public MethodWrapper(ChildMapping<MethodNode> method){
        this.node = method;
    }

    public ChildMapping<MethodNode> getNode(){
        return this.node;
    }

    public MethodWrapper setMapping(String mappedName, String mappedDesc){
        if(this.node != null){
            this.node.setMapped(mappedName, mappedDesc);
        }
        return this;
    }

    public AbsNode getLastNode(int opCode){
        if(this.node != null){
            AbstractInsnNode last = null;
            for(AbstractInsnNode node : this.node.getNode().instructions.toArray()){
                if(node.getOpcode() == opCode){
                    last = node;
                }
            }
            return new AbsNode(last);
        }
        return new AbsNode(null);
    }

}
