package de.canitzp.libloader.remap;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * @author canitzp
 */
public class CustomClassRemapper extends ClassRemapper {

    public CustomClassRemapper(ClassVisitor cv, Remapper remapper) {
        super(cv, remapper);
    }

}
