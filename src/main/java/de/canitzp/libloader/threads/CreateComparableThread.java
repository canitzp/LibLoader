package de.canitzp.libloader.threads;

import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.Mappings;
import de.canitzp.libloader.remap.MappingsParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class CreateComparableThread extends Thread {

    @Override
    public void run() {
        MappingsParser.compareMode = true;
        MappingsParser.nameOverride = "comparable.mappings";
        new GenMappingsThread().run();
        MappingsParser.compareMode = false;
        MappingsParser.nameOverride = null;
    }
}
