package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.Hex;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/*************************************************
 * This is here just as an example, your solution
 * probably needs to use different message types
 *************************************************/
public class BroadcastMessage extends ProtoMessage {

    public final static short MSG_ID = 103;

    private final int instance;
    private final Operation op;

    public BroadcastMessage(int instance, Operation op) {
        super(MSG_ID);
        this.instance = instance;
        this.op = op;
    }

    public int getInstance() {
        return instance;
    }


    public Operation getOp() {
        return op;
    }

    @Override
    public String toString() {
        return "BroadcastMessage{" +
                "instance=" + instance +
                ", op=" + op +
                '}';
    }

    public static ISerializer<BroadcastMessage> serializer = new ISerializer<BroadcastMessage>() {
        @Override
        public void serialize(BroadcastMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);

            out.writeInt(msg.op.getData().length);
            out.writeBytes(msg.op.getData());

            out.writeInt(msg.op.getKey().length());
            out.writeBytes(msg.op.getKey().getBytes(StandardCharsets.UTF_8));

            out.writeByte(msg.op.getOpType());
        }

        @Override
        public BroadcastMessage deserialize(ByteBuf in) {
            int instance = in.readInt();

            int data_size = in.readInt();
            byte[] data = in.readBytes(data_size).array();

            int opID_size = in.readInt();
            String opID = in.readBytes(opID_size).toString();

            byte opType = in.readByte();

            return new BroadcastMessage(instance, new Operation(opType, opID, data));
        }
    };

}
