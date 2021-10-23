package rkernel.signal;

import rkernel.BasicKernel;
import rkernel.IKernel;
import rkernel.component.IComponent;
import rkernel.exception.FileManagerException;
import rkernel.exception.SignalRegistryException;
import rkernel.utils.file.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class SignalManager implements ISignalManager{

    protected static SignalRegistry registry;
    protected IKernel kernel;

    private final File documentroot = Path.of(".").toFile();
    private final String registriesDirectory = "/registries/";

    public SignalManager(IKernel kernel) {
        this.kernel = kernel;
        try {
            if (!FileManager.getInstance(documentroot).pathExist(getRegistryPath())){
                registry = new SignalRegistry(kernel.getName());
                flush(registry);
            }
            registry = getRegistry();
        } catch (FileManagerException | IOException e) {
            kernel.dispatchLogException(e);
        }
    }

    public Object findInterpreter(String type){
        List<SignalRegistry.SignalTypeEntry> entries = registry.getSignalTypeEntries()
                .stream()
                .filter(signalTypeEntry -> signalTypeEntry.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        if (!entries.isEmpty()){
            if (entries.get(0).getComponentName() != null && !entries.get(0).getComponentName().isEmpty()){
                return kernel.findComponentByName(entries.get(0).getComponentName());
            } else if (entries.get(0).getKernelName() != null && !entries.get(0).getKernelName().isEmpty()){
                return kernel.findKernelByName(entries.get(0).getKernelName());
            } else{
                return null;
            }
        }else{
            return null;
        }
    }

    private SignalRegistry getRegistry() throws FileManagerException, IOException {
        return (SignalRegistry) FileManager.getInstance(documentroot)
                .getFileContent(getRegistryPath());
    }

    public void setKernel(BasicKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public SignalRegistry.SignalTypeEntry getRegistryEntry(String type) {
        return registry.getTypeEntry(type);
    }

    private void flush(SignalRegistry registry) throws FileManagerException {
        try {
            FileManager.getInstance(documentroot).writeFileContent(registry, getRegistryPath());
        } catch (IOException e) {
            kernel.dispatchLogException(e);
        }
    }

    public Path getRegistryPath(){
        if (!Files.exists(Path.of(documentroot.getPath().concat(registriesDirectory)))){
            try {
                Files.createDirectories(Path.of(documentroot.getPath().concat(registriesDirectory)));
            } catch (IOException e) {
                kernel.dispatchLogException(e);
            }
        }
        return Path.of(documentroot.getPath().concat("/registries/"+ kernel.getName().replace(" ", "_") +".xml"));
    }

    public void addSignalType(String type, IComponent component) throws SignalRegistryException {
        registry.addSignalType(type, component);
        try {
            flush(registry);
        } catch (FileManagerException e) {
            throw new SignalRegistryException(e.getMessage());
        }
    }

    public void addSignalType(String type, IKernel kernel) throws SignalRegistryException {
        registry.addSignalType(type, kernel);
        try {
            flush(registry);
        } catch (FileManagerException e) {
            throw new SignalRegistryException(e.getMessage());
        }
    }

    public void removeSignalType(String type) throws SignalRegistryException {
        registry.removeSignalType(type);
        try {
            flush(registry);
        } catch (FileManagerException e) {
            throw new SignalRegistryException(e.getMessage());
        }
    }
}
