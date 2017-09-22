package de.canitzp.libloader.remap;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author canitzp
 */
public class CustomClassRemapper extends RemappingClassAdapter {

    private CustomRemapper remapper;

    public CustomClassRemapper(ClassVisitor cv, CustomRemapper remapper) {
        super(cv, remapper);
        this.remapper = remapper;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(this.remapper.mapFieldAccess(access, className, name, desc), name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.visitMethod(this.remapper.mapMethodAccess(access, className, name, desc), name, desc, signature, exceptions);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, this.remapper.mapInnerClassName(name, outerName, innerName), access);
    }

    public void finish(ClassNode node) {
        ClassMapping mapping = this.remapper.getClassMappingForName(node.name);
        if(mapping != null){
            for (ChildMapping<MethodNode> method : mapping.getMethods()) {
                if(method.getParamNames() != null){
                    for(MethodNode methodNode : node.methods){
                        if (method.isSameAs(methodNode)) {
                            if (methodNode.localVariables != null) {
                                String[] paramNames = method.getParamNames();
                                for (int i = 0; i < paramNames.length; i++) {
                                    methodNode.localVariables.get(Modifier.isStatic(methodNode.access) ? i : i+1).name = paramNames[i];
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
