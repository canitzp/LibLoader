package de.canitzp.libloader;

/**
 * @author canitzp
 */
public enum Names {

    MAIN("client/main/Main"),
    MINECRAFT("client/Minecraft"),
    SNOOPER("profiler/Snooper"),
    RESOURCE_LOCATION("util/ResourceLocation")
    ;

    private String classDefName;
    Names(String classDefName){
        this.classDefName = classDefName;
    }

    public String getClassDefName() {
        return "net/minecraft/" + classDefName;
    }

    public String getClassname(){
        return this.getClassDefName().replace("/", ".");
    }

    public String getDescName(){
        return "L" + this.getClassDefName() + ";";
    }

    @Override
    public String toString() {
        return this.getClassDefName();
    }
}
