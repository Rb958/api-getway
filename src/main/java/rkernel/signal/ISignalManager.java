/*#################################################################################################
 # Copyright (c) 2021 Afrikpay                                                                    #
 #################################################################################################*/

package rkernel.signal;

import rkernel.BasicKernel;
import rkernel.IKernel;
import rkernel.component.IComponent;
import rkernel.exception.SignalRegistryException;

public interface ISignalManager {

    Object findInterpreter(String type) throws SignalRegistryException;

    void addSignalType(String type, IComponent component) throws SignalRegistryException;

    void addSignalType(String type, IKernel kernel) throws SignalRegistryException;

    void removeSignalType(String type) throws SignalRegistryException;

    void setKernel(BasicKernel kernel) throws SignalRegistryException;

    SignalRegistry.SignalTypeEntry getRegistryEntry(String test);
}
