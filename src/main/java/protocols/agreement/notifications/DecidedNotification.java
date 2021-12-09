package protocols.agreement.notifications;

import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

public class DecidedNotification extends ProtoNotification {

    public static final short NOTIFICATION_ID = 101;

    private final int instance;
    private final Operation operation;

    public DecidedNotification(int instance, Operation operation) {
        super(NOTIFICATION_ID);
        this.instance = instance;
        this.operation = operation;
    }

    public int getInstance() {
        return instance;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return "DecidedNotification{" +
                "instance=" + instance +
                ", operation=" + operation +
                '}';
    }
}
