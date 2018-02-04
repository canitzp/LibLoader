package de.canitzp.libloader.launch;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author canitzp
 */
public interface ITransformer {

    String getClassName();

    default void transformClass(ClassNode classNode){}

    default void transformMethod(ClassNode classNode, MethodNode methodNode){}

    default void transformField(ClassNode classNode, FieldNode fieldNode){}

}
