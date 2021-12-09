package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

public class AcceptMessage extends ProtoMessage {

    public static final short MSG_ID = 104;

    private int instance;
    private int proposer_seq;
    private byte[] value;

    public AcceptMessage(int instance, int proposer_seq, byte[] value) {
        super(MSG_ID);
        this.instance = instance;
        this.proposer_seq = proposer_seq;
        this.value = value;
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
                ", value=" + value +
                '}';
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public int getInstance() {
        return instance;
    }

    public byte[] getValue() {
        return value;
    }

    public static ISerializer<AcceptMessage> serializer = new ISerializer<AcceptMessage>() {
        @Override
        public void serialize(AcceptMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);
            out.writeBytes(msg.value);
        }

        @Override
        public AcceptMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            byte[] value = in.array();
            return new AcceptMessage(instance, proposer_seq, value);
        }
    };

}
