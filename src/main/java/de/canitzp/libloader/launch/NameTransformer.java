package de.canitzp.libloader.launch;

import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.CustomClassRemapper;
import de.canitzp.libloader.remap.CustomRemapper;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author canitzp
 */
public class NameTransformer implements IClassNameTransformer, IClassTransformer {

    public static CustomRemapper remapper;

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
            //cn.accept(cw);
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

}
