package protocols.agreement.multipaxos.messages;

import io.netty.buffer.ByteBuf;
import protocols.agreement.multipaxos.OperationLog;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*************************************************
 * This is here just as an example, your solution
 * probably needs to use different message types
 *************************************************/
public class MPBroadcastMessage extends ProtoMessage {

    public final static short MSG_ID = 203;

    private final int instance;
    private final OperationLog op;

    public MPBroadcastMessage(int instance, OperationLog op) {
        super(MSG_ID);
        this.instance = instance;
        this.op = op;
    }

    public int getInstance() {
        return instance;
    }


    public OperationLog getOp() {
        return op;
    }

    @Override
    public String toString() {
        return "BroadcastMessage{" +
                "instance=" + instance +
                ", op=" + op +
                '}';
    }

    public static ISerializer<MPBroadcastMessage> serializer = new ISerializer<MPBroadcastMessage>() {
        @Override
        public void serialize(MPBroadcastMessage msg, ByteBuf out) throws IOException {
            out.writeInt(msg.instance);
            OperationLog.serializer.serialize(msg.getOp(), out);
        }

        @Override
        public MPBroadcastMessage deserialize(ByteBuf in) throws IOException {
            int instance = in.readInt();
            OperationLog op = OperationLog.serializer.deserialize(in);
            return new MPBroadcastMessage(instance, op);
        }
    };

}
