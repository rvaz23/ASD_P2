package protocols.agreement.requests;

import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;
import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

public class ProposeRequest extends ProtoRequest {

    public static final short REQUEST_ID = 101;

    private final int instance;
    private final String opId;
    private final Operation operation;
    private int handicap;

    public ProposeRequest(int instance, String opId, Operation operation,int handicap) {
        super(REQUEST_ID);
        this.instance = instance;
        this.opId = opId;
        this.operation = operation;
        this.handicap= handicap;
    }

    public int getInstance() {
        return instance;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getOpId() {
        return opId;
    }

    public int getHandicap() {
        return handicap;
    }

    public void setHandicap(int handicap) {
        this.handicap = handicap;
    }

    @Override
    public String toString() {
        return "ProposeRequest{" +
                "instance=" + instance +
                ", opId=" + opId +
                ", operation=" + operation +
                '}';
    }
}
