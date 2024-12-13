package utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import exceptions.AutomationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class Reflections {
    static final Logger logger = LogManager.getLogger(Reflections.class);

    public static Class<?> findClassByName(String className) {
        List<Class<?>> classes;
        try {
            classes = Reflections.getAllClasses();
            for (Class<?> clazz : classes) {
                if(clazz.getSimpleName().equalsIgnoreCase(className)){
                    logger.info(String.format("Localizado a Classe %s por reflections", className));
                    return clazz;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new AutomationException("ClassNotFoundException:" + e.getMessage());
        } catch (IOException e) {
            throw new AutomationException("IOException:" + e.getMessage());
        }

        throw new AutomationException("Não foi localizado a classe %s utilizando reflections", className);
    }

    private static List<Class<?>> getAllClasses() throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        List<Class<?>> classes = new ArrayList<>();
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            scanClasses("", file, classes);
        }
        return classes;
    }

    private static void scanClasses(String packageName, File directory, List<Class<?>> classes) throws ClassNotFoundException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String subPackageName = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                        scanClasses(subPackageName, file, classes);
                    } else if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    }
                }
            }
        }
    }
}
