package protocols.agreement.multipaxos.messages;

import io.netty.buffer.ByteBuf;
import protocols.agreement.multipaxos.OperationLog;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MPPrepareOkMessage extends ProtoMessage {

    public static final short MSG_ID = 206;

    private int instance;
    private int proposer_seq;
    private int highest_seq;
    private OperationLog highest_val;

    public MPPrepareOkMessage(int instance, int proposer_seq, int highest_seq, OperationLog highest_val) {
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
        return "PrepareMessageOk{" +
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

    public OperationLog getHighest_val() {
        return highest_val;
    }

    public int getHighest_seq() {
        return highest_seq;
    }

    public static ISerializer<MPPrepareOkMessage> serializer = new ISerializer<MPPrepareOkMessage>() {
        @Override
        public void serialize(MPPrepareOkMessage msg, ByteBuf out) throws IOException {
            out.writeInt(msg.instance);
            out.writeInt(msg.proposer_seq);
            out.writeInt(msg.highest_seq);


            if (msg.highest_val!=null){
                out.writeByte(1);
                OperationLog.serializer.serialize(msg.getHighest_val(), out);
            }else{
                out.writeByte(0);
            }

        }

        @Override
        public MPPrepareOkMessage deserialize(ByteBuf in) throws IOException {
            int instance = in.readInt();
            int proposer_seq = in.readInt();
            int highest_seq = in.readInt();

            byte exists=in.readByte();

            OperationLog highest_val=new OperationLog();
            if (exists==1){
                highest_val = OperationLog.serializer.deserialize(in);
            }


            return new MPPrepareOkMessage(instance, proposer_seq, highest_seq, highest_val);
        }
    };

}
