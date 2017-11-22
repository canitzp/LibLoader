package de.canitzp.libloader.launch.transformer;

import de.canitzp.libloader.launch.ITransformer;
import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;

/**
 * @author canitzp
 */
public class BasicMethodInvoke implements ITransformer {

    private String className, methodName, methodDesc, invokeClass, invokeMethod;

    public BasicMethodInvoke(String className, String methodName, String methodDesc, String invokeClass, String invokeMethod){
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.invokeClass = invokeClass;
        this.invokeMethod = invokeMethod;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if(methodNode.name.equals(this.methodName) &&  methodNode.desc.equals(this.methodDesc)){
            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, this.invokeClass, this.invokeMethod, "(Ljava/lang/Object;)V", false));
            methodNode.instructions.insert(list);
        }
    }

}
