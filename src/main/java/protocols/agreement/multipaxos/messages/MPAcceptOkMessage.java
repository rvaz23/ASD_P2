package protocols.agreement.multipaxos.messages;

import io.netty.buffer.ByteBuf;
import protocols.agreement.multipaxos.OperationLog;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MPAcceptOkMessage extends ProtoMessage {

    public static final short MSG_ID = 202;

    private int instance;
    private int proposer_seq;
    private OperationLog value;

    public MPAcceptOkMessage(int instance, int proposer_seq, OperationLog value) {
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

    public OperationLog getValue() {
        return value;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public static ISerializer<MPAcceptOkMessage> serializer = new ISerializer<MPAcceptOkMessage>() {
        @Override
        public void serialize(MPAcceptOkMessage msg, ByteBuf out) throws IOException {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);

            if (msg.value != null) {
                out.writeByte(1);
                OperationLog.serializer.serialize(msg.getValue(), out);

            } else {
                out.writeByte(0);
            }
        }

        @Override
        public MPAcceptOkMessage deserialize(ByteBuf in) throws IOException {
            int instance = in.readInt();
            int proposer_seq = in.readInt();

            byte exists = in.readByte();

            OperationLog val=new OperationLog();
            if (exists == 1) {
               val = OperationLog.serializer.deserialize(in);
            }

            return new MPAcceptOkMessage(instance, proposer_seq, val);
        }
    };

}
