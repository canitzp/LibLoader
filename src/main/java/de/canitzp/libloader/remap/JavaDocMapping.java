package de.canitzp.libloader.remap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class JavaDocMapping {

    private List<String> lines = new ArrayList<>();

    public JavaDocMapping addLine(String line){
        lines.add(line);
        return this;
    }

    public List<String> finish(){
        List<String> ret = new ArrayList<>();
        ret.add("/**");
        for(String s : this.lines){
            ret.add(" * " + s);
        }
        ret.add(" */");
        return ret;
    }

}
