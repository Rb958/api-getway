package rkernel;

import rkernel.component.IComponentLoader;
import rkernel.exception.FileManagerException;
import rkernel.exception.SignalRegistryException;
import rkernel.exception.UnImplementedMethod;
import rkernel.utils.file.FileAdapter;
import rkernel.utils.file.FileEvent;
import rkernel.utils.file.FileManager;
import rkernel.utils.file.FileWatcher;

import java.io.File;
import java.io.IOException;
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
            IKernel tmpKernel = (IKernel) KernelFactory.getInstance(kernelClass);
            if (!tmpKernel.isDefault()) {
                tmpKernel.getSignalType().forEach(signalType -> {
                    try {
                        kernel.getSignalManager().addSignalType(signalType, tmpKernel);
                    } catch (SignalRegistryException e) {
                        kernel.dispatchLogException(e);
                    }
                });
                if(!tmpKernel.isRunning()) {
                    tmpKernel.load();
                    System.out.println(tmpKernel.getName() + " has successfully loaded");
                }
                kernel.addKernel(tmpKernel);
            }
        } catch (IOException | UnImplementedMethod e) {
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
        System.out.println("Start watching folder "+ watchedDirectory.getAbsolutePath() + "...");
        FileWatcher kernelWatch = new FileWatcher(watchedDirectory);
        kernelWatch.addEventListener(new FileAdapter() {
            @Override
            public void onCreateFile(FileEvent event) {
                new Thread(()->{
                    System.out.println("New file detected : "+ event.getFile().getName());
                    executeClass(event.getFile());
                }).start();
            }
        }).watch();
    }
}
