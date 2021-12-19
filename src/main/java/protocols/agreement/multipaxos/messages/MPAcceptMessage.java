package protocols.agreement.multipaxos.messages;

import io.netty.buffer.ByteBuf;
import protocols.agreement.multipaxos.OperationLog;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;

public class MPAcceptMessage extends ProtoMessage {

    public static final short MSG_ID = 201;

    private int instance;
    private int proposer_seq;
    private OperationLog value;

    public MPAcceptMessage(int instance, int proposer_seq, OperationLog value) {
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
        return "AcceptMessage{" +
                "instance=" + instance +
                ", proposer_seq=" + proposer_seq +
                ", value=" + value +
                '}';
    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public void setProposer_seq(int proposer_seq) {
        this.proposer_seq = proposer_seq;
    }

    public OperationLog getValue() {
        return value;
    }

    public void setValue(OperationLog value) {
        this.value = value;
    }

    public static ISerializer<MPAcceptMessage> serializer = new ISerializer<MPAcceptMessage>() {
        @Override
        public void serialize(MPAcceptMessage msg, ByteBuf out) throws IOException {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);

            if (msg.value != null) {
                out.writeByte((byte)1);
                OperationLog.serializer.serialize(msg.getValue(), out);
            } else {
                out.writeByte((byte)0);
            }
        }

        @Override
        public MPAcceptMessage deserialize(ByteBuf in) throws IOException {
            int instance = in.readInt();
            int proposer_seq = in.readInt();

            byte exists = in.readByte();
            OperationLog val = new OperationLog();

            if(exists == 1){
                val = OperationLog.serializer.deserialize(in);
            }

            return new MPAcceptMessage(instance, proposer_seq, val);
        }
    };

}
