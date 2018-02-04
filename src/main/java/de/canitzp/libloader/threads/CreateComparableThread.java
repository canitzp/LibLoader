package de.canitzp.libloader.threads;

import de.canitzp.libloader.remap.MappingsParser;

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
