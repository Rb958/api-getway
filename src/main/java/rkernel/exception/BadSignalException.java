package rkernel.exception;

public class BadSignalException extends GeneralException {
    public BadSignalException(int code, String message) {
        super(code, message);
    }
}
