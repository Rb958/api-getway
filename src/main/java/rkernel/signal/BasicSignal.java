package rkernel.signal;

import java.util.Objects;

public class BasicSignal<T> {
    protected String type;
    protected T payload;

    public BasicSignal(String type, T payload){
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicSignal<?> that = (BasicSignal<?>) o;
        return type.equals(that.type) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, payload);
    }

    @Override
    public String toString() {
        return "BasicSignal{" +
                "type='" + type + '\'' +
                ", payload=" + payload +
                '}';
    }
}
