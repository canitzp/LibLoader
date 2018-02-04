package de.canitzp.libloader.remap.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author canitzp
 */
public class Scripts {

    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<String, Class<?>>(){{
        put("classes", ScriptClasses.class);
    }};

    public static Class<?> findClass(String s){
        return CLASSES.getOrDefault(s, null);
    }

    public static Class<?> runTask(Class<?> clazz, String taskName, List<Object> args) {
        if(clazz != null){
            try {
                Method method = clazz.getMethod(taskName, args.getClass());
                return (Class<?>) method.invoke(null, args);
            } catch (NoSuchMethodException e) {
                System.out.println("No task method for '" + taskName + "' found inside of " + clazz);
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                System.out.println("Task '" + taskName + "' in '" + clazz + "' couldn't be invoked or returned a non Class<?> object!");
                e.printStackTrace();
            }
        } else {
            System.out.println("The given container class for task: " + taskName + " with arguments: " + args.toString() + " is null!");
        }
        return null;
    }

}
