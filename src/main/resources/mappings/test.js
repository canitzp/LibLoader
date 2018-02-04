var dep = ClassPool.get("net.minecraft.client.main.Main");
var mainMethod = ClassPool.getMethod(dep, "main");

mainMethod.getNode().setParamNames("args"); // set argument name

var node = mainMethod.getLastNode(OPC.INVOKEVIRTUAL); // get last INVOKEVIRTUAL node in method
var minecraft = ClassPool.add(ClassPool.raw(node.getOwner()).setMapping("net/minecraft/client/Minecraft")); // get the classmapping from obf name and set the corresponding mapped name
ClassPool.getMethod(minecraft, node.getName(), node.getDesc()).setMapping("run", "()V"); // remapping the invoked method


