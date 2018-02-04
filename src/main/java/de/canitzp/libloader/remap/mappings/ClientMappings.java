package de.canitzp.libloader.remap.mappings;

import de.canitzp.libloader.remap.ChildMapping;
import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.JavaDocMapping;
import de.canitzp.libloader.remap.Mappings;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.canitzp.libloader.Names.*;

/**
 * @author canitzp
 */
public class ClientMappings extends MappingsBase {

    public static void processMain(){
        ClassMapping main = Mappings.getClassMappingFromMappedName(MAIN);
        if(isNotNull(main)){
            main.setJavaDoc(new JavaDocMapping().addLine("This is the entry for the game. Only LibLoader and the Launchwrapper are accessed earlier"));
            List<ChildMapping<MethodNode>> methods = Mappings.getMethodFromObf(main, "main", "([Ljava/lang/String;)V");
            if(methods.size() == 1){
                ChildMapping<MethodNode> method = methods.get(0);
                Mappings.addMethodMapping(main, method.setParamNames("args"), method.getObfuscatedName(), method.getObfuscatedDesc());
            }
        }
    }

    @MappingsDependsOn("processMain")
    public static void findMinecraft(){
        ClassMapping main = Mappings.getClassMappingFromMappedName(MAIN);
        ChildMapping<MethodNode> mainMethod = Mappings.getMethodFromMapped(main, "main", "([Ljava/lang/String;)V");
        if(isNotNull(main, mainMethod)){
            AbstractInsnNode insnNode = Mappings.getOpcodeSequence(mainMethod.getNode().instructions, Opcodes.NEW, Opcodes.DUP, Opcodes.ALOAD, Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL);
            if(insnNode instanceof MethodInsnNode){
                if(Mappings.addMappingsWithMethodInsnNode((MethodInsnNode) insnNode, MINECRAFT.getClassDefName(), "run", "()V")){
                    ClassMapping cm = Mappings.getClassMappingFromMappedName(MINECRAFT).setJavaDoc(new JavaDocMapping().addLine("This is the runtime class for Minecraft"));
                    Mappings.getMethodFromMapped(cm, "run", "()V").setJavaDoc(new JavaDocMapping().addLine("The Minecraft gameloop"));
                }
            }
        }
    }

    @MappingsDependsOn("findMinecraft")
    public static void processMinecraft() {
        ClassMapping minecraft = Mappings.getClassMappingFromMappedName(MINECRAFT);
        ChildMapping<MethodNode> clinit = Mappings.getMethodFromMapped(minecraft, "<clinit>", "()V");
        if (isNotNull(minecraft, clinit)) {
            for (ChildMapping<FieldNode> field : minecraft.getFields()) {
                if ("Lorg/apache/logging/log4j/Logger;".equals(field.getObfuscatedDesc())) {
                    Mappings.addFieldMapping(minecraft, field, "LOGGER", "Lorg/apache/logging/log4j/Logger;");
                } else if ("[B".equals(field.getObfuscatedDesc())) {
                    Mappings.addFieldMapping(minecraft, field, "memoryReserve", "[B");
                } else if ("Ljava/util/List<Lorg/lwjgl/opengl/DisplayMode;>;".equals(field.getNode().signature)) {
                    Mappings.addFieldMapping(minecraft, field, "macDisplayModes", "Ljava/util/List;");
                }
            }
            AbstractInsnNode insnNode = Mappings.getOpcodeSequence(clinit.getNode().instructions, Opcodes.NEW, Opcodes.DUP, Opcodes.LDC, Opcodes.INVOKESPECIAL);
            if (insnNode != null && insnNode.getPrevious() instanceof LdcInsnNode && insnNode.getNext() instanceof FieldInsnNode) {
                if ("textures/gui/title/mojang.png".equals(((LdcInsnNode) insnNode.getPrevious()).cst)) {
                    Mappings.addClassMapping(Type.getType(((FieldInsnNode) insnNode.getNext()).desc).getClassName(), RESOURCE_LOCATION.getClassDefName());
                    Mappings.addFieldMapping(minecraft, minecraft.getFieldByName(((FieldInsnNode) insnNode.getNext()).name), "LOGO_LOCATION", RESOURCE_LOCATION.getDescName());
                }
            } else {
                System.out.println("Can't find class ResourceLocation and Minecraft.LOGO_LOCATION");
            }
            ChildMapping<MethodNode> init = minecraft.getMethodByName("<init>");
            Mappings.addClassMapping(Type.getArgumentTypes(init.getObfuscatedDesc())[0].getClassName(), "net/minecraft/client/GameConfiguration");
            Mappings.addMethodMapping(minecraft, init.setParamNames("configuration"), "<init>", "(Lnet/minecraft/client/GameConfiguration;)V");
            List<LdcInsnNode> nodes = Mappings.getLdcNodes(init.getNode().instructions);
            for (LdcInsnNode ldcNode : nodes) {
                if (ldcNode != null && "client".equals(ldcNode.cst)) {
                    MethodInsnNode methodNode = (MethodInsnNode) ldcNode.getNext().getNext();
                    ClassMapping server = Mappings.getClassMappingFromObfName(methodNode.owner);
                    Mappings.addMethodMapping(server, Mappings.getMethodFromObf(server, methodNode.name, methodNode.desc).get(0), "getCurrentTimeMillis", null);
                    FieldInsnNode fieldNode = (FieldInsnNode) methodNode.getNext().getNext();
                    Mappings.addClassMapping(Type.getType(fieldNode.desc).getClassName(), SNOOPER.getClassDefName());
                    Mappings.addFieldMapping(minecraft, minecraft.getFieldByName(fieldNode.name), "usageSnooper", SNOOPER.getDescName());
                }
            }
            for (String iface : minecraft.getClassNode().interfaces) {
                ClassMapping mapping = Mappings.getClassMappingFromObfName(iface);
                if (mapping != null) {
                    if (mapping.getMethods().size() == 2) {
                        Mappings.addClassMapping(mapping.getObfName(), "net/minecraft/util/IThreadListener");
                        for (ChildMapping<MethodNode> method : mapping.getMethods()) {
                            if (Type.getArgumentTypes(method.getObfuscatedDesc()).length > 0) {
                                Mappings.addMethodMapping(mapping, method, "addScheduledTask", null);
                            } else {
                                Mappings.addMethodMapping(mapping, method, "isCallingFromMinecraftThread", null);
                            }
                        }
                    } else if(mapping.getMethods().size() == 3){
                        Mappings.addClassMapping(mapping.getObfName(), "net/minecraft/profiler/ISnooperInfo");
                        for (ChildMapping<MethodNode> method : mapping.getMethods()) {
                            if (Type.getArgumentTypes(method.getObfuscatedDesc()).length == 0) {
                                Mappings.addMethodMapping(mapping, method, "isSnooperEnabled", null);
                                method.setJavaDoc(new JavaDocMapping().addLine("Returns whether snooping is enabled or not."));
                            }
                        }
                    } else {
                        Mappings.addClassMapping(mapping.getObfName(), "net/minecraft/util/DefaultInputHandler");
                        for (ChildMapping<MethodNode> method : mapping.getMethods()) {
                            if (Type.getArgumentTypes(method.getObfuscatedDesc()).length == 0) {
                                Mappings.addMethodMapping(mapping, method, "getDefaultInputHandler", null);
                            }
                        }
                    }
                }
            }
            List<ChildMapping<MethodNode>> methods = Mappings.getMethodByStrings(minecraft, "LWJGL Version: {}", "server-resource-packs", "Startup");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "init", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Couldn't set pixel format");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "createDisplay", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "icons/icon_16x16.png", "icons/icon_32x32.png", "Couldn't set icon");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "setWindowIcon", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "is64Bit", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Timer hack thread");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "startTimerHackThread", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "#@!@# Game crashed! Crash report saved to: #@!@# ", "#@?@# Game crashed! Crash report could not be saved. #@?@#");
            if(methods.size() == 1){
                ChildMapping<MethodNode> method = methods.get(0);
                Mappings.addClassMapping(Type.getArgumentTypes(method.getObfuscatedDesc())[0].getClassName(), "net/minecraft/crash/CrashReport");
                Mappings.addMethodMapping(minecraft, method.setParamNames("crashReport"), "displayCrashReport", "(Lnet/minecraft/crash/CrashReport;)V");
            }
            methods = Mappings.getMethodByStrings(minecraft, "Caught error stitching, removing all assigned resourcepacks");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "refreshResources", null);
            }
            methods = Mappings.getMethodsByDesc(minecraft, "(Ljava/io/InputStream;)Ljava/nio/ByteBuffer;");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("imageStream"), "readImageToBuffer", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "logo", "Unable to load logo: {}");
            if(methods.size() == 1){
                ChildMapping<MethodNode> method = methods.get(0);
                Mappings.addClassMapping(Type.getArgumentTypes(method.getObfuscatedDesc())[0].getClassName(), "net/minecraft/client/renderer/texture/TextureManager");
                Mappings.addMethodMapping(minecraft, method.setParamNames("textureManager"), "drawSplashScreen", "(Lnet/minecraft/client/renderer/texture/TextureManager;)V");
            }
            methods = Mappings.getMethodByStrings(minecraft, "########## GL ERROR ##########");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("message"), "checkGLError", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Stopping!");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "shutdownMinecraftApplet", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "scheduledExecutables", "preRenderErrors", "toasts");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "runGameLoop", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "display_update");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "updateDisplay", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, ".", "unspecified");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("keyCount"), "updateDebugProfilerName", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "##0.00", "unspecified", "ROOT ", "] ");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("elapsedTicksTime"), "displayDebugInfo", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Couldn't toggle fullscreen");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "toggleFullscreen", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "gui", "gameMode", "textures", "Affected screen");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "runTick", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Manually triggered debug crash");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0), "runTickKeyboard", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "debug.reload_chunks.message", "debug.creative_spectator.error", "debug.reload_resourcepacks.message");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("auxKey"), "processKeyF3", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "", "debug.prefix", " ");
            if(methods.size() == 1){
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("untranslatedTemplate", "objs"), "debugFeedbackTranslated", null);
            }
            methods = Mappings.getMethodByStrings(minecraft, "Starting integrated server", "Level ID", "menu.loadingLevel");
            if(methods.size() == 1){
                ChildMapping<MethodNode> method = methods.get(0);
                Mappings.addClassMapping(Type.getArgumentTypes(method.getObfuscatedDesc())[2].getClassName(), "net/minecraft/world/WorldSettings");
                Mappings.addMethodMapping(minecraft, methods.get(0).setParamNames("folderName", "worldName", "worldSettings"), "launchIntegratedServer", "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V");
            }
        }
    }

}
