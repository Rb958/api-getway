package rkernel.component;

import rkernel.IKernel;
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
        File[] files = folder.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                executeClass(file);
            }
        }
    }

    private void executeClass(File file){
        new Thread(() -> {
            try {
                Class<?> tmpClass = loadSingleFile(file, IComponent.class);
                Constructor<?> constructor = tmpClass.getConstructor();
                IComponent component = (IComponent) constructor.newInstance();
                component.getSIgnalType().forEach(signalType ->{
                    try {
                        defaultKernel.getSignalManager().addSignalType(signalType, component);
                    } catch (SignalRegistryException e) {
                        defaultKernel.dispatchLogException(e);
                    }
                });
                defaultKernel.getComponents().put(component.getName(), component);
                component.load(defaultKernel);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException e) {
                defaultKernel.dispatchLogException(e);
            }
        }).start();
    }

    @Override
    public void watch(File watchDirectotie) {
        FileWatcher componentWatcher = new FileWatcher(watchDirectotie);
        componentWatcher.addEventListener(new FileAdapter() {
            @Override
            public void onCreateFile(FileEvent event) {
                executeClass(event.getFile());
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
