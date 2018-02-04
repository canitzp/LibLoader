package de.canitzp.mappings;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * @author canitzp
 */
public class AbsNode {

    private AbstractInsnNode node;

    public AbsNode(AbstractInsnNode node) {
        this.node = node;
    }

    public boolean isFieldInsNode(){
        return this.node instanceof FieldInsnNode;
    }

    public FieldInsnNode getAsFieldInsNode(){
        return this.isFieldInsNode() ? ((FieldInsnNode) this.node) : null;
    }

    public boolean isMethodInsNode(){
        return this.node instanceof MethodInsnNode;
    }

    public MethodInsnNode getAsMethodInsNode(){
        return this.isMethodInsNode() ? ((MethodInsnNode) this.node) : null;
    }

    public boolean isTypeInsNode(){
        return this.node instanceof TypeInsnNode;
    }

    public TypeInsnNode getAsTypeInsNode(){
        return this.isTypeInsNode() ? ((TypeInsnNode) this.node) : null;
    }

    public String getOwner(){
        if(this.isFieldInsNode()){
            return this.getAsFieldInsNode().owner;
        } else if(this.isMethodInsNode()){
            return this.getAsMethodInsNode().owner;
        }
        return null;
    }

    public String getName(){
        if(this.isFieldInsNode()){
            return this.getAsFieldInsNode().name;
        } else if(this.isMethodInsNode()){
            return this.getAsMethodInsNode().name;
        }
        return null;
    }

    public String getDesc(){
        if(this.isFieldInsNode()){
            return this.getAsFieldInsNode().desc;
        }else if(this.isMethodInsNode()){
            return this.getAsMethodInsNode().desc;
        } else if(this.isTypeInsNode()){
            return this.getAsTypeInsNode().desc;
        }
        return null;
    }
}
