package protocols.statemachine.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

public class RPCMessage extends ProtoMessage {

    public static final short MSG_ID = 201;

    private int lastDecided;

    public RPCMessage(int lastDecided) {
        super(MSG_ID);
        this.lastDecided = lastDecided;
    }

    @Override
    public short getId() {
        return super.getId();
    }

    public int getLastDecided() {
        return this.lastDecided;
    }

    @Override
    public String toString() {
        return "PrepareMessage{" +
                "lastDecided=" + lastDecided +
                '}';
    }

    public static ISerializer<RPCMessage> serializer = new ISerializer<RPCMessage>() {
        @Override
        public void serialize(RPCMessage msg, ByteBuf out) {
            out.writeInt(msg.lastDecided);
        }

        @Override
        public RPCMessage deserialize(ByteBuf in) {
            int lastDecided = in.readInt();
            return new RPCMessage(lastDecided);
        }
    };

}
