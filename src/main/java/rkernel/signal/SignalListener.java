package rkernel.signal;

import java.util.EventListener;

public interface SignalListener extends EventListener {
    void onProcessIncoming(SignalEvent<?> signalEvent);
}
