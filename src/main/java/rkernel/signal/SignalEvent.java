package rkernel.signal;

import java.util.EventObject;

public class SignalEvent<T> extends EventObject {
    public SignalEvent(BasicSignal<T> source) {
        super(source);
    }

    public BasicSignal<?> getSignal(){
        return (BasicSignal<?>) getSource();
    }
}
