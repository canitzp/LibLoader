package de.canitzp.libloader.remap.mappings;

import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.Mappings;

import java.util.List;

/**
 * @author canitzp
 */
public class CommonMappings extends MappingsBase {

    public static void processMinecraftServer(){

    }

    @MappingsDependsOn("processMinecraftServer")
    public static void findSeveralStuff(){
        // Block
        List<ClassMapping> mappings = Mappings.findClassWithString("air", "stone", "grass", "log2", "Block{", "}");
        if(mappings.size() == 1){
            Mappings.addClassMapping(mappings.get(0).getObfName(), "net/minecraft/block/Block");
        } else {
            Mappings.throwRemappingError("Couldn't find net/minecraft/block/Block");
        }
        // Blocks
        mappings = Mappings.findClassWithString("air", "stone", "grass", "end_portal", "Accessed Blocks before Bootstrap!");
        if(mappings.size() == 1){
            Mappings.addClassMapping(mappings.get(0).getObfName(), "net/minecraft/init/Blocks");
        } else {
            Mappings.throwRemappingError("Couldn't find net/minecraft/init/Blocks");
        }
        // Item
        mappings = Mappings.findClassWithString("lefthanded", "cooldown", "iron_shovel", "flint_and_steel", "golden_leggings");
        if(mappings.size() == 1){
            Mappings.addClassMapping(mappings.get(0).getObfName(), "net/minecraft/item/Item");
        } else {
            Mappings.throwRemappingError("Couldn't find net/minecraft/item/Item");
        }
        // Items
        mappings = Mappings.findClassWithString("Accessed Items before Bootstrap!", "iron_shovel", "flint_and_steel", "golden_leggings");
        if(mappings.size() == 1){
            Mappings.addClassMapping(mappings.get(0).getObfName(), "net/minecraft/init/Items");
        } else {
            Mappings.throwRemappingError("Couldn't find net/minecraft/init/Items");
        }
        // ItemStack
        mappings = Mappings.findClassWithString("#.##", "id", "Count", "tag", "minecraft:air", "Unbreakable");
        if(mappings.size() == 1){
            Mappings.addClassMapping(mappings.get(0).getObfName(), "net/minecraft/item/ItemStack");
        } else {
            Mappings.throwRemappingError("Couldn't find net/minecraft/item/ItemStack");
        }
    }

}
