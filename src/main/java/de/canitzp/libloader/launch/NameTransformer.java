package de.canitzp.libloader.launch;

import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.CustomClassRemapper;
import de.canitzp.libloader.remap.CustomRemapper;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class NameTransformer implements IClassNameTransformer, IClassTransformer {

    public static CustomRemapper remapper;
    public static Map<String, Class<?>> insertionInterfaces = new HashMap<String, Class<?>>(){{

    }};

    public static boolean isMinecraftClass(String className){
        return (!className.contains(".") && !className.contains("/")) || className.contains("net.minecraft") || className.contains("net/minecraft");
    }

    @Override // return with dots
    public String unmapClassName(String name) {
        if(isMinecraftClass(name)){
            return remapper.unmapClassName(name, false);
        }
        return name;
    }

    @Override // return with dots
    public String remapClassName(String name) {
        if(isMinecraftClass(name)){
            return remapper.remapClassName(name, false);
        }
        return name;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(isMinecraftClass(name) || isMinecraftClass(transformedName)){
            ClassMapping mapping = remapper.getClassMappingForName(name);
            if(mapping != null){
                for(ITransformer transformer : Tweaker.TRANSFORMER.getOrDefault(transformedName.replace(".", "/"), new ArrayList<>())){
                    mapping.addClassTransformer(transformer);
                }
                ClassReader cr = new ClassReader(basicClass);
                ClassNode cn = new ClassNode();
                CustomClassRemapper ccr = new CustomClassRemapper(cn, remapper);
                cr.accept(ccr, ClassReader.EXPAND_FRAMES);
                mapping.setClassNode(cn).setClassReader(cr);
                return writeDebug(mapping.getRawData(), transformedName);
            } else System.out.println(name + "   " + transformedName);


            /*



            ClassMapping mapping = new ClassMapping().setObfName(name).setMappedName(transformedName).setClassReader(cr).setClassNode(cn);
            for(MethodNode mn : cn.methods){
                mapping.addMethod(new ChildMapping<>(mn));
            }
            for(FieldNode fn: cn.fields){
                mapping.addField(new ChildMapping<>(fn));
            }
            for(ITransformer transformer : Tweaker.TRANSFORMER.getOrDefault(transformedName, new ArrayList<>())){
                mapping.addClassTransformer(transformer);
            }
            remapper.mappings.add(mapping);
            return writeDebug(mapping.getRawData(), transformedName);
            */

            //ccr.finish(cn);
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(basicClass);
            CustomClassRemapper ccr = new CustomClassRemapper(cn, remapper);
            cr.accept(ccr, ClassReader.EXPAND_FRAMES);
            ccr.finish(cn);
            //cn.accept(cw);
            ClassWriter cw = null;
            if(insertionInterfaces.containsKey(transformedName)){
                try {
                    cw = applyInterfaceInsertion(cn, insertionInterfaces.get(transformedName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                cw = new ClassWriter(0);
                cn.accept(cw);
            }
            return writeDebug(cw != null ? cw.toByteArray() : basicClass, transformedName);
        }
        return basicClass;
    }

    private byte[] writeDebug(byte[] ary, String name){
        if(Tweaker.DEBUG){
            try {
                FileUtils.writeByteArrayToFile(new File(Tweaker.debugDirectory, name.replace(".", File.separator) + ".class"), ary);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ary;
    }

    private ClassWriter applyInterfaceInsertion(ClassNode cn, Class<?> insertion) throws IOException {
        cn.interfaces.add(insertion.getName());
        ClassReader cr = new ClassReader(insertion.getName());
        ClassNode insertionNode = new ClassNode();
        cr.accept(insertionNode, 0);
        List<MethodNode> toBuild = new ArrayList<>();
        for(MethodNode iMethod : insertionNode.methods){
            MethodNode matchingMethods = null;
            for(MethodNode method : cn.methods){
                if(method.desc.equals(iMethod.desc) && method.name.equals(iMethod.name)){
                    matchingMethods = method;
                    break;
                }
            }
            if(matchingMethods != null){
                if(matchingMethods.visibleAnnotations == null){
                    matchingMethods.visibleAnnotations = new ArrayList<>();
                }
                matchingMethods.visibleAnnotations.add(new AnnotationNode("Ljava/lang/Override;"));
            } else {
                toBuild.add(iMethod);
            }
        }
        if(!toBuild.isEmpty()){
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            for(MethodNode method : toBuild){
                MethodVisitor constructor = cw.visitMethod(method.access - Opcodes.ACC_ABSTRACT, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
                constructor.visitAnnotation("Ljava/lang/Override;", true);
                //constructor.visitLdcInsn("test");
                constructor.visitVarInsn(Opcodes.ALOAD, 0);
                constructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cn.name, "j", method.desc, false);
                constructor.visitInsn(Opcodes.ARETURN);
                //
                //
                //constructor.visitInsn(Opcodes.RETURN);
                constructor.visitMaxs(1, 1);
                constructor.visitEnd();
            }
            return cw;
        }
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw;
    }

}

/*
L0
    LINENUMBER 284 L0
    ALOAD 0
    ALOAD 1
    ICONST_1
    INVOKEVIRTUAL net/minecraft/world/World.isBlockLoaded (Lnet/minecraft/util/math/BlockPos;Z)Z
    IRETURN
   L1
    LOCALVARIABLE this Lnet/minecraft/world/World; L0 L1 0
    LOCALVARIABLE pos Lnet/minecraft/util/math/BlockPos; L0 L1 1
    MAXSTACK = 3
    MAXLOCALS = 2
 */