package de.canitzp.libloader.remap.mappings;

/**
 * @author canitzp
 */
public class MappingsBase {

    public static boolean isNotNull(Object... objects){
        for(Object o : objects){
            if(o == null) return false;
        }
        return true;
    }

}
