package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.nio.charset.StandardCharsets;

public class AcceptOkMessage extends ProtoMessage {

    public static final short MSG_ID = 104;

    private int instance;
    private int proposer_seq;
    private Operation value;

    public AcceptOkMessage(int instance, int proposer_seq, Operation value) {
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

    public int getInstance() {
        return instance;
    }

    public Operation getValue() {
        return value;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public static ISerializer<AcceptOkMessage> serializer = new ISerializer<AcceptOkMessage>() {
        @Override
        public void serialize(AcceptOkMessage msg, ByteBuf out) {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);

            out.writeInt(msg.value.getData().length);
            out.writeBytes(msg.value.getData());

            out.writeInt(msg.value.getKey().length());
            out.writeBytes(msg.value.getKey().getBytes(StandardCharsets.UTF_8));

            out.writeByte(msg.value.getOpType());
        }

        @Override
        public AcceptOkMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();

            int data_size = in.readInt();
            byte[] data = in.readBytes(data_size).array();

            int opID_size = in.readInt();
            String opID = in.readBytes(opID_size).toString();

            byte opType = in.readByte();

            return new AcceptOkMessage(instance, proposer_seq, new Operation(opType, opID, data));
        }
    };

}
