package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AcceptMessage extends ProtoMessage {

    public static final short MSG_ID = 101;

    private int instance;
    private int proposer_seq;
    private Operation value;

    public AcceptMessage(int instance, int proposer_seq, Operation value) {
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

    public Operation getValue() {
        return value;
    }

    public void setValue(Operation value) {
        this.value = value;
    }

    public static ISerializer<AcceptMessage> serializer = new ISerializer<AcceptMessage>() {
        @Override
        public void serialize(AcceptMessage msg, ByteBuf out) {
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
        public AcceptMessage deserialize(ByteBuf in) {
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

            return new AcceptMessage(instance, proposer_seq, val);
        }
    };

}
