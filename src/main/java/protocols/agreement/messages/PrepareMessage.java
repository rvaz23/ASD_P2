package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.Hex;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.util.UUID;

public class PrepareMessage extends ProtoMessage {

    public static final short MSG_ID = 102;

    private int instance;
    private int proposer_seq;

    public PrepareMessage(int instance, int proposer_seq) {
        super(MSG_ID);
        this.instance = instance;
        this.proposer_seq = proposer_seq;
    }

    @Override
    public short getId() {
        return super.getId();
    }

    @Override
    public String toString() {
        return "PrepareMessage{" +
                "instance=" + instance +
                ", proposer_seq=" + proposer_seq +
                '}';
    }

    public static ISerializer<PrepareMessage> serializer = new ISerializer<PrepareMessage>() {
        @Override
        public void serialize(PrepareMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);
        }

        @Override
        public PrepareMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            return new PrepareMessage(instance, proposer_seq);
        }
    };

}
