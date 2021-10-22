package rkernel.component;

import rkernel.IKernel;
import rkernel.exception.FileManagerException;
import rkernel.exception.SignalRegistryException;
import rkernel.utils.file.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class BasicComponentLoader implements IComponentLoader<IComponent>{

    private final HashMap<String, IComponent> components;
    private IKernel defaultKernel;

    public BasicComponentLoader(){
        this.components = new HashMap<>();
    }

    public void setKernel(IKernel defaultKernel) {
        this.defaultKernel = defaultKernel;
    }

    public void loadComponents(File folder){
        try {
            File[] files = FileManager.getInstance(folder).getFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println(file.getName());
                    Class<?> componentClass = loadSingleFile(file, IComponent.class);
                    if (componentClass != null)
                        executeClass(componentClass);
                }
            }
        }catch (IOException | FileManagerException e) {
            e.printStackTrace();
        }
    }

    private void executeClass(Class<?> tmpClass){
        try {
            System.out.println("Execute Component ... ");
            System.out.println("rkernel : " + defaultKernel.getName());
            Constructor<?> constructor = tmpClass.getConstructor();
            IComponent component = (IComponent) constructor.newInstance();
            defaultKernel.getSignalType().addAll(component.getSIgnalType());
            defaultKernel.getComponents().put(component.getName(), component);
            component.load(defaultKernel);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            defaultKernel.dispatchLogException(e);
        }
    }

    @Override
    public void watch(File watchDirectotie) {
        FileWatcher componentWatcher = new FileWatcher(watchDirectotie);
        componentWatcher.addEventListener(new FileAdapter() {
            @Override
            public void onCreateFile(FileEvent event) {
                try {
                    Class<?> componentClass = loadSingleFile(event.getFile(), IComponent.class);
                    executeClass(componentClass);
                } catch (IOException e) {
                    defaultKernel.dispatchLogException(e);
                }
            }

            @Override
            public void onDeleteFile(FileEvent event) {
                wipeComponent(event.getFile());
            }
        }).watch();
    }

    private void wipeComponent(File file) {
        try{
            Class<?> componentClass = loadSingleFile(file, IComponent.class);
            Constructor<?> constructor = componentClass.getConstructor();
            IComponent component = (IComponent) constructor.newInstance();
            if (defaultKernel.getComponents().containsKey(component.getName())) {
                Collection<String> signalTypes = component.getSIgnalType();
                signalTypes.forEach(signalType -> {
                    try {
                        defaultKernel.getSignalManager().removeSignalType(signalType);
                    } catch (SignalRegistryException e) {
                        defaultKernel.dispatchLogException(e);
                    }
                });
                defaultKernel.getComponents().remove(component.getName());
            }
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            defaultKernel.dispatchLogException(e);
        }
    }

    public HashMap<String, IComponent> getComponents() {
        return components;
    }
}
