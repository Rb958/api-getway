package rkernel;

import rkernel.component.BasicComponentLoader;
import rkernel.component.IComponent;
import rkernel.component.IComponentLoader;
import rkernel.exception.UnImplementedMethod;
import rkernel.signal.BasicSignal;
import rkernel.signal.ISignalManager;
import rkernel.signal.SignalManager;
import rkernel.signal.basic.LoggingSignal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class BasicKernel implements IKernel{

    protected SignalManager signalManager;
    protected IComponentLoader<IComponent> componentLoader;
    protected IComponentLoader<IKernel> kernelLoader;
    protected Map<String, IKernel> kernels;
    protected Map<String, IComponent> components;
    protected Collection<String> signals;
    protected boolean running;

    protected String kernelName = "DefaultKernel";

    public BasicKernel(Builder builder) {
        this.componentLoader = builder.getComponentLoader();
        this.kernelLoader = builder.getKernelLoader();
        this.kernels = builder.getKernels();
        this.signals = builder.getSignals();
        this.components = new HashMap<>();
        this.kernelName = builder.name;
    }

    public BasicKernel() {
        this.componentLoader = new BasicComponentLoader();
        this.kernelLoader = new BasicKernelLoader();
        this.kernels = new HashMap<>();
        this.components = new HashMap<>();
        this.signals = new ArrayList<>();
    }

    @Override
    public synchronized void load() {
        try {
            this.running = true;
            signalManager = new SignalManager(this);
            signals = signalManager.retrieveKernelsSignals();
            File file = Paths.get(".").toFile();
            Path componentPath = Paths.get("components/".concat(kernelName));
            if (Files.notExists(componentPath))
                Files.createDirectories(componentPath);
            File componentFile = componentPath.toFile();
            if (componentLoader != null) {
                new Thread(() -> {
                    componentLoader.loadComponents(componentFile);
                    componentLoader.watch(componentFile);
                }).start();
            }
            if (kernelLoader != null) {
                new Thread(() -> {
                    kernelLoader.setKernel(this);
                    kernelLoader.loadComponents(file);
                    kernelLoader.watch(file);
                }).start();
            }
        } catch (IOException e) {
            dispatchLogException(e);
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
    public Object dispatchSignal(BasicSignal<?> signal) {
        Object response = null;
        try {
            // Find Interpreter
            Object interpreter = getInterpreterOf(signal.getType());
            // Call Interpreter with his data
            if (interpreter instanceof IComponent) {
                response = ((IComponent) interpreter).processSignal(signal);
            } else if (interpreter instanceof IKernel) {
                System.out.println("Interpreter : " + ((IKernel) interpreter).getName());
                response = ((IKernel) interpreter).processSignal(signal);
            }
        } catch (UnImplementedMethod unImplementedMethod) {
            dispatchLogException(unImplementedMethod);
        }
        return response;
    }

    @Override
    public Object processSignal(BasicSignal<?> signal) throws UnImplementedMethod {
        throw new UnImplementedMethod();
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
        return signalManager.findInterpreter(signalType);
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

    @Override
    public boolean isRunning() {
        return running;
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
            this.name = "DefaultKernel";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicKernel that = (BasicKernel) o;
        return kernelName.equals(that.kernelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kernelName);
    }
}
