package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.nio.charset.StandardCharsets;

public class AcceptOkMessage extends ProtoMessage {

    public static final short MSG_ID = 102;

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
        return "AcceptOkMessage{" +
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

            if (msg.value != null) {
                out.writeByte(1);
                out.writeByte(msg.value.getOpType());

                out.writeInt(msg.value.getKey().length());
                out.writeBytes(msg.value.getKey().getBytes(StandardCharsets.UTF_8));

                int dataSize = msg.value.getData().length;

                out.writeInt(dataSize);
                out.writeBytes(msg.value.getData());

            } else {
                out.writeByte(0);
            }
        }

        @Override
        public AcceptOkMessage deserialize(ByteBuf in) {
            int instance = in.readInt();
            int proposer_seq = in.readInt();

            byte exists = in.readByte();

            Operation val=null;
            if (exists == 1) {
                byte opType = in.readByte();

                int opID_size = in.readInt();
                byte[] opID_array = new byte[opID_size];
                in.readBytes(opID_array);
                String opID = new String(opID_array, StandardCharsets.UTF_8);

                int data_size = in.readInt();
                byte[] data = new byte[data_size];
                in.readBytes(data);
                val =new Operation(opType, opID, data);
            }

            return new AcceptOkMessage(instance, proposer_seq, val);
        }
    };

}
