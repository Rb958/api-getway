package rkernel;

import rkernel.component.IComponentLoader;
import rkernel.exception.FileManagerException;
import rkernel.exception.SignalRegistryException;
import rkernel.utils.file.FileAdapter;
import rkernel.utils.file.FileEvent;
import rkernel.utils.file.FileManager;
import rkernel.utils.file.FileWatcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BasicKernelLoader implements IComponentLoader<IKernel> {
    protected final HashMap<String, IKernel> kernels = new HashMap<>();
    protected IKernel kernel;

    @Override
    public void loadComponents(File folder) {
        try {
            File[] files = FileManager.getInstance(folder).getFiles();
            if (files != null) {
                for (File file : files) {
                    executeClass(file);
                }
            }
        }catch (FileManagerException e) {
            kernel.dispatchLogException(e);
        }
    }

    protected void executeClass(File file) {
        try {
            Class<?> kernelClass = loadSingleFile(file, IKernel.class);
            Constructor<?> constructor = kernelClass.getConstructor();
            IKernel tmpKernel = (IKernel) constructor.newInstance();
            if (!tmpKernel.isDefault()) {
                tmpKernel.getSignalType().forEach(signalType -> {
                    try {
                        kernel.getSignalManager().addSignalType(signalType, tmpKernel);
                    } catch (SignalRegistryException e) {
                        kernel.dispatchLogException(e);
                    }
                });
                kernel.addKernel(tmpKernel);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException e) {
            kernel.dispatchLogException(e);
        }
    }

    @Override
    public HashMap<String, IKernel> getComponents() {
        return kernels;
    }

    @Override
    public void setKernel(IKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void watch(File watchedDirectory) {
        FileWatcher kernelWatch = new FileWatcher(watchedDirectory);
        kernelWatch.addEventListener(new FileAdapter() {
            @Override
            public void onCreateFile(FileEvent event) {
                executeClass(event.getFile());
            }
        }).watch();
    }
}
