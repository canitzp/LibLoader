package de.canitzp.libloader.launch.transformer;

import de.canitzp.libloader.launch.ITransformer;
import de.canitzp.libloader.remap.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.InvocationTargetException;

/**
 * @author canitzp
 */
public class InjectLoadingStages implements ITransformer {

    @Override
    public String getClassName() {
        return "net/minecraft/client/Minecraft";
    }

    @Override
    public void transformMethod(ClassNode classNode, MethodNode methodNode) {
        if("init".equals(methodNode.name) && "()V".equals(methodNode.desc)){
            MethodInsnNode insnNode = Mappings.findInvokeNode(methodNode.instructions, this.getClassName(), "refreshResources", "()V");
            if(insnNode != null){
                methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
                methodNode.instructions.set(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, this.getClass().getName().replace(".", "/"), "preInit", "(Ljava/lang/Object;)V", false));
            }
        }
    }

    public static void preInit(Object minecraft){
        System.out.println("Pre init " + minecraft);
        try {
            minecraft.getClass().getDeclaredMethod("refreshResources").invoke(minecraft);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
