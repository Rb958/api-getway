package rkernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class KernelFactory{
    private static final KernelFactory instance = new KernelFactory();
    private final Map<Class<?>, Object> mapHolder = new HashMap<>();
    private KernelFactory(){}

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> kernelClass) {
        synchronized (instance){
            if (!instance.mapHolder.containsKey(kernelClass)) {
                try {
                Constructor<?> constructor = kernelClass.getConstructor();
                T kernel = (T) constructor.newInstance();
                instance.mapHolder.put(kernelClass, kernel);
                    System.out.println("Create instance of " + kernelClass.getName());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return (T) instance.mapHolder.get(kernelClass);
        }
    }
}
