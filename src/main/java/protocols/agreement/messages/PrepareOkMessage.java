package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

public class PrepareOkMessage extends ProtoMessage {

    public static final short MSG_ID = 103;

    private int instance;
    private int proposer_seq;
    private int highest_seq;
    private byte[] highest_val;

    public PrepareOkMessage(int instance, int proposer_seq, int highest_seq, byte[] highest_val) {
        super(MSG_ID);
        this.instance = instance;
        this.proposer_seq = proposer_seq;
        this.highest_seq = highest_seq;
        this.highest_val = highest_val;
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
                ", highest_seq=" + highest_seq +
                ", highest_val=" + highest_val +
                '}';
    }

    public int getInstance() {
        return instance;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public byte[] getHighest_val() {
        return highest_val;
    }

    public int getHighest_seq() {
        return highest_seq;
    }

    public static ISerializer<PrepareOkMessage> serializer = new ISerializer<PrepareOkMessage>() {
        @Override
        public void serialize(PrepareOkMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);
            out.writeInt(msg.highest_seq);
            out.writeBytes(msg.highest_val);
        }

        @Override
        public PrepareOkMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            int highest_seq = in.readInt();
            byte[] highest_val = in.array();
            return new PrepareOkMessage(instance, proposer_seq, highest_seq, highest_val);
        }
    };

}
