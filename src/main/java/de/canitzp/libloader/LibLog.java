package de.canitzp.libloader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author canitzp
 */
public class LibLog {

    public static void msg(Level level, String msg, boolean showDateAndTime){
        String s = level != null ? "[" + level.getLocalizedName() + "]" : "";
        if(showDateAndTime){
            s += new SimpleDateFormat("'['dd.MM.YYYY HH:mm:ss']'").format(new Date());
        }
        System.out.println(s + " " + msg);
    }

    public static void msg(String msg, boolean showDateAndTime){
        msg(null, msg, showDateAndTime);
    }

    public static void msg(Level level, String msg){
        msg(level, msg, true);
    }

    public static void infoSilence(String msg){
        msg(null, msg, true);
    }

}
