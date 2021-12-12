package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.nio.charset.StandardCharsets;

public class PrepareOkMessage extends ProtoMessage {

    public static final short MSG_ID = 103;

    private int instance;
    private int proposer_seq;
    private int highest_seq;
    private Operation highest_val;

    public PrepareOkMessage(int instance, int proposer_seq, int highest_seq, Operation highest_val) {
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

    public Operation getHighest_val() {
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

            out.writeByte(msg.highest_val.getOpType());

            out.writeInt(msg.highest_val.getKey().length());
            out.writeBytes(msg.highest_val.getKey().getBytes(StandardCharsets.UTF_8));

            out.writeInt(msg.highest_val.getData().length);
            out.writeBytes(msg.highest_val.getData());
        }

        @Override
        public PrepareOkMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            int highest_seq = in.readInt();

            byte opType = in.readByte();

            int keySize = in.readInt();
            String key = in.readBytes(keySize).toString(StandardCharsets.UTF_8);

            int opSize = in.readInt();
            byte[] op = in.readBytes(opSize).array();

            Operation highest_val = new Operation(opType,key,op);
            return new PrepareOkMessage(instance, proposer_seq, highest_seq, highest_val);
        }
    };

}
