package rkernel.component;

import rkernel.IKernel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public interface IComponentLoader<T> {

    void loadComponents(File folder);

    HashMap<String, T> getComponents();

    default Class<?> loadSingleFile(File file, Class<?> interClass) throws IOException {
        if (!file.exists()) {
            return null;
        }
        Class<?> componentClass = null;
        URL url = file.toPath().toUri().toURL();
        try(URLClassLoader loader = new URLClassLoader(new URL[]{url}); JarFile jarFile = new JarFile(file.getAbsolutePath())) {
            Enumeration<JarEntry> enumeration = jarFile.entries();
            boolean found = false;
            int iterator = 0;
            while (enumeration.hasMoreElements() && !found) {
                String tmpClassName = enumeration.nextElement().toString();
                if (tmpClassName.endsWith(".class")) {
                    tmpClassName = tmpClassName.substring(0, tmpClassName.length() - 6);
                    tmpClassName = tmpClassName.replace('/', '.');
                    Class<?> tmpClass = Class.forName(tmpClassName, true, loader);
                    if (tmpClass != interClass) {
                        List<Class<?>> classes = Arrays.stream(tmpClass.getInterfaces())
                                .filter(tmpInter -> tmpInter == interClass)
                                .collect(Collectors.toList());
                        if (!classes.isEmpty()) {
                            componentClass = tmpClass;
                            found = true;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return componentClass;
    }

    void setKernel(IKernel kernel);

    void watch(File watchDirectory);
}
