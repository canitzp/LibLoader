package de.canitzp.libloader.remap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author canitzp
 */
public class JavaDocMapping {

    public static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\\*/\\*/");

    private List<String> lines = new ArrayList<>();

    public JavaDocMapping addLine(String line){
        lines.add(line);
        return this;
    }

    public String getRawLines() {
        String raw = "";
        for(String s : this.lines){
            raw += s + LINE_SPLIT_PATTERN.pattern();
        }
        return raw;
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
