/*#################################################################################################
 # Copyright (c) 2021 Richie AKAWA                                                                #
 #################################################################################################*/

package rkernel.signal;

import jakarta.xml.bind.annotation.*;
import rkernel.IKernel;
import rkernel.component.IComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SignalRegistry {

    @XmlElement(name = "Kernel", type = String.class)
    protected String kernelName;
    @XmlElementWrapper(name = "SignalTypeEntries")
    @XmlElement(name = "SignalTypeEntry")
    protected final List<SignalTypeEntry> signalTypeEntries;

    public SignalRegistry() {
        signalTypeEntries = new ArrayList<>();
    }

    public SignalRegistry(String kernelName) {
        signalTypeEntries = new ArrayList<>();
        this.kernelName = kernelName;
    }

    public String getKernelName() {
        return kernelName;
    }

    public void setKernelName(String kernelName) {
        this.kernelName = kernelName;
    }

    public List<SignalTypeEntry> getSignalTypeEntries() {
        return signalTypeEntries;
    }

    public SignalTypeEntry getTypeEntry(String type) {
        List<SignalTypeEntry> entries = signalTypeEntries.stream()
                .filter(signalTypeEntry -> signalTypeEntry.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
        return entries.isEmpty() ? null : entries.get(0);
    }

    public void addSignalType(String type, IComponent component){
        List<SignalTypeEntry> tmpEntries = signalTypeEntries.stream()
                .filter(registryEntry -> registryEntry.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
        if (!tmpEntries.isEmpty()){
            signalTypeEntries.removeAll(tmpEntries);
        }
        SignalTypeEntry entry = new SignalTypeEntry();
        entry.componentName = component.getName();
        entry.type = type;
        signalTypeEntries.add(entry);
    }

    public void addSignalType(String type, IKernel kernel){
        List<SignalTypeEntry> tmpEntries = signalTypeEntries.stream()
                .filter(registryEntry -> registryEntry.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
        if (!tmpEntries.isEmpty()){
            signalTypeEntries.removeAll(tmpEntries);
        }
        SignalTypeEntry entry = new SignalTypeEntry();
        entry.kernelName = kernel.getName();
        entry.type = type;
        signalTypeEntries.add(entry);
    }

    public void removeSignalType(String type){
        List<SignalTypeEntry> tmpEntries = signalTypeEntries.stream()
                .filter(registryEntry -> registryEntry.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
        if (!tmpEntries.isEmpty()){
            signalTypeEntries.removeAll(tmpEntries);
        }
    }

    @XmlType(propOrder = {"type","componentName", "kernelName"})
    @XmlRootElement(name = "SignalTypeEntry")
    static class SignalTypeEntry {
        private String type;
        private String componentName;
        private String kernelName;

        public SignalTypeEntry() {
        }

        @XmlAttribute
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlAttribute
        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        @XmlAttribute
        public String getKernelName() {
            return kernelName;
        }

        public void setKernelName(String kernelName) {
            this.kernelName = kernelName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SignalTypeEntry entry = (SignalTypeEntry) o;
            return type.equals(entry.type) && Objects.equals(componentName, entry.componentName) && Objects.equals(kernelName, entry.kernelName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, componentName, kernelName);
        }

        @Override
        public String toString() {
            return "Type => " +
                    ((componentName != null) ? "ComponentName => "+ componentName : "") +
                    ((kernelName != null) ? "KernelName => "+ kernelName : "")
                    ;
        }
    }

    @Override
    public String toString() {
        return "KernelName[" + kernelName + "]" +
                " => SignalType[" + signalTypeEntries +"]";
    }
}
