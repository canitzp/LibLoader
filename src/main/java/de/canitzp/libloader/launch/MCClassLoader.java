package de.canitzp.libloader.launch;

import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.CustomClassRemapper;
import de.canitzp.libloader.remap.CustomRemapper;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
public class MCClassLoader extends URLClassLoader {

    public static List<ClassMapping> mappings = new ArrayList<>();
    private Map<String, Class> cachedClasses = new HashMap<>();

    public MCClassLoader(URL[] urls) {
        super(urls, null);
        NameTransformer.remapper = new CustomRemapper(mappings);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if(NameTransformer.isMinecraftClass(name)){
            if(cachedClasses.containsKey(name)){
                return cachedClasses.get(name);
            }
            byte[] data = getClassBytes(name);
            if(data != null){
                String mappedName = getMappedName(name);
                System.out.println("Found data for " + mappedName);
                ClassReader cr = new ClassReader(data);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                cr.accept(new CustomClassRemapper(cw, NameTransformer.remapper), ClassReader.EXPAND_FRAMES);
                data = cw.toByteArray();
                Class<?> c = defineClass(mappedName.replace("/", "."), data, 0, data.length);
                cachedClasses.put(mappedName, c);
                cachedClasses.put(name, c);
                return c;
            }
        }
        return super.findClass(name);
    }

    private String getMappedName(String name){
        for(ClassMapping mapping : mappings){
            if(mapping.hasMappedName()){
                if(name.equals(mapping.getObfName()) || name.equals(mapping.getMappedName())){
                    return mapping.getMappedName();
                }
            }
        }
        return name;
    }

    public void addURL(URL url){
        super.addURL(url);
    }

    public byte[] getClassBytes(String name){
        String path = name.replace('.', '/').concat(".class");
        InputStream is = getResourceAsStream(path);
        if(is != null){
            try {
                return IOUtils.toByteArray(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
