package rkernel.component;

import rkernel.IKernel;
import rkernel.signal.BasicSignal;
import rkernel.signal.SignalListener;

import java.util.Collection;


public interface IComponent {

    void load(IKernel kernel);

    String getName();

    Collection<BasicSignal<?>> getSignals();

    void addSignalListener(SignalListener signalListener);

    boolean isRunning();

    void stop();

    Collection<String> getSIgnalType();

    Object processSignal(BasicSignal<?> signal);
}
