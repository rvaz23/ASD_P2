package protocols.agreement.multipaxos.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

public class MPPrepareMessage extends ProtoMessage {

    public static final short MSG_ID = 205;

    private int instance;
    private int proposer_seq;

    public MPPrepareMessage(int instance, int proposer_seq) {
        super(MSG_ID);
        this.instance = instance;
        this.proposer_seq = proposer_seq;
    }

    @Override
    public short getId() {
        return super.getId();
    }

    public int getInstance() {
        return instance;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    @Override
    public String toString() {
        return "PrepareMessage{" +
                "instance=" + instance +
                ", proposer_seq=" + proposer_seq +
                '}';
    }

    public static ISerializer<MPPrepareMessage> serializer = new ISerializer<MPPrepareMessage>() {
        @Override
        public void serialize(MPPrepareMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);
        }

        @Override
        public MPPrepareMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            return new MPPrepareMessage(instance, proposer_seq);
        }
    };

}
