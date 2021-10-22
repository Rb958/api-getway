package rkernel;

import rkernel.component.IComponent;
import rkernel.component.IComponentLoader;
import rkernel.signal.BasicSignal;
import rkernel.signal.ISignalManager;
import rkernel.signal.SignalManager;
import rkernel.signal.basic.LoggingSignal;
import rkernel.utils.file.FileWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class BasicKernel implements IKernel{

    private SignalManager signalManager;
    private final IComponentLoader<IComponent> componentLoader;
    private final IComponentLoader<IKernel> kernelLoader;
    private final Map<String, IKernel> kernels;
    private final Map<String, IComponent> components;
    private final Collection<String> signals;
    private FileWatcher componentWatcher;
    private FileWatcher kernelWatcher;
    private boolean running;

    private final String kernelName;

    BasicKernel(Builder builder) {
        this.componentLoader = builder.getComponentLoader();
        this.kernelLoader = builder.getKernelLoader();
        this.kernels = builder.getKernels();
        this.signals = builder.getSignals();
        this.running = false;
        this.components = new HashMap<>();
        this.kernelName = builder.name;
    }

    @Override
    public void load() {
        try {
            signalManager = new SignalManager(this);
            File file = Path.of(".").toFile();
            Path componentPath = Path.of("components/".concat(kernelName));
            if (Files.notExists(componentPath))
                Files.createDirectories(componentPath);
            File componentFile = componentPath.toFile();
            if (componentLoader != null) {
                System.out.println("Load Component");
                componentLoader.loadComponents(componentFile);
                componentLoader.watch(componentFile);
            }
            if (kernelLoader != null) {
                kernelLoader.loadComponents(file);
                kernelLoader.watch(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return kernelName;
    }

    @Override
    public Map<String, IComponent> getComponents() {
        return components;
    }

    @Override
    public Collection<IKernel> dispatchSignal(BasicSignal<?> signal) {
        List<IKernel> tmpKernel = new ArrayList<>();
        this.kernels.forEach((name, kernel) -> {
            if (kernel.getInterpreterOf(signal.getType()) != null){
                kernel.processSignal(signal);
                tmpKernel.add(kernel);
            }
        });
        return tmpKernel;
    }

    @Override
    public void processSignal(BasicSignal<?> signal) {
        // Find Interpreter
        Object interpreter = getInterpreterOf(signal.getType());

        // Call Interpreter with his data
        if (interpreter instanceof IComponent){
            ((IComponent) interpreter).processSignal(signal);
        }else if (interpreter instanceof IKernel){
            ((IKernel) interpreter).processSignal(signal);
        }
    }

    @Override
    public Map<String,IKernel> getKernels() {
        return kernels;
    }

    @Override
    public Collection<String> getSignalType() {
        return signals;
    }

    @Override
    public Object getInterpreterOf(String signalType) {
        if(signals.contains(signalType)){
            return signalManager.findInterpreter(signalType);
        }else{
            return null;
        }
    }

    @Override
    public IComponent findComponentByName(String componentName) {
        return components.getOrDefault(componentName, null);
    }

    @Override
    public IKernel findKernelByName(String kernelName) {
        return kernels.getOrDefault(kernelName, null);
    }

    @Override
    public ISignalManager getSignalManager() {
        return signalManager;
    }

    @Override
    public void addKernel(IKernel kernel) {
        kernels.put(kernel.getName(), kernel);
    }

    @Override
    public void dispatchLogException(Exception e) {
        LoggingSignal loggingSignal = new LoggingSignal(e);
        this.dispatchSignal(loggingSignal);
    }

    public static final class Builder{
        private IComponentLoader<IComponent> componentLoader;
        private IComponentLoader<IKernel> kernelLoader;
        private final Map<String, IKernel> kernels;
        private final Collection<String> signals;
        private String name;

        public Builder() {
            this.componentLoader = null;
            this.kernelLoader = null;
            this.kernels = new HashMap<>();
            this.signals = new ArrayList<>();
            this.name = "Default rkernel";
        }

        IComponentLoader<IComponent> getComponentLoader() {
            return componentLoader;
        }

        public Builder setComponentLoader(IComponentLoader<IComponent> componentLoader) {
            this.componentLoader = componentLoader;
            return this;
        }

        IComponentLoader<IKernel> getKernelLoader() {
            return kernelLoader;
        }

        public Builder setKernelLoader(IComponentLoader<IKernel> kernelLoader) {
            this.kernelLoader = kernelLoader;
            return this;
        }

        public Builder setName(String name){
            this.name = name;
            return this;
        }

        Map<String, IKernel> getKernels() {
            return kernels;
        }

        Collection<String> getSignals() {
            return signals;
        }

        public BasicKernel build(){
            BasicKernel kernel = new BasicKernel(this);
            this.componentLoader.setKernel(kernel);
            return kernel;
        }
    }
}
