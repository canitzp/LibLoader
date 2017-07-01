package de.canitzp.libloader.remap.mappings;

import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.JavaDocMapping;
import de.canitzp.libloader.remap.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * @author canitzp
 */
public class ClientMappings extends MappingsBase {

    public static void processMain(){
        ClassMapping main = Mappings.getClassMappingFromMappedName("net/minecraft/client/main/Main");
        if(isNotNull(main)){
            List<ChildMapping<MethodNode>> methods = Mappings.getMethodFromObf(main, "main", "([Ljava/lang/String;)V");
            if(methods.size() == 1){
                ChildMapping<MethodNode> method = methods.get(0);
                Mappings.addMethodMapping(main, method, method.getObfuscatedName(), method.getObfuscatedDesc());
            }
        }
    }

    @MappingsDependsOn("processMain")
    public static void findMinecraft(){
        ClassMapping main = Mappings.getClassMappingFromMappedName("net/minecraft/client/main/Main");
        ChildMapping<MethodNode> mainMethod = Mappings.getMethodFromMapped(main, "main", "([Ljava/lang/String;)V");
        if(isNotNull(main, mainMethod)){
            AbstractInsnNode insnNode = Mappings.getOpcodeSequence(mainMethod.getNode().instructions, Opcodes.NEW, Opcodes.DUP, Opcodes.ALOAD, Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL);
            if(insnNode instanceof MethodInsnNode){
                if(Mappings.addMappingsWithMethodInsnNode((MethodInsnNode) insnNode, "net/minecraft/client/Minecraft", "run", "()V")){
                    ClassMapping cm = Mappings.getClassMappingFromMappedName("net/minecraft/client/Minecraft").setJavaDoc(new JavaDocMapping().addLine("This is the runtime class for Minecraft"));
                    Mappings.getMethodFromMapped(cm, "run", "()V").setJavaDoc(new JavaDocMapping().addLine("The Minecraft gameloop"));
                }
            }
        }
    }

}
