/*#################################################################################################
 # Copyright (c) 2021 Afrikpay                                                                    #
 #################################################################################################*/

package rkernel.signal.basic;

import rkernel.signal.BasicSignal;

public final class LoggingSignal extends BasicSignal<Exception> {
    public LoggingSignal(Exception payload) {
        super("exception_logging", payload);
    }
}
