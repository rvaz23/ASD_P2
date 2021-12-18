package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotifyMessage extends ProtoMessage {

    public static final short MSG_ID = 104;

    private int instance;
    private List<Host> membership;
    private Map<Integer, Operation> decided;
    private byte[] state;

    public NotifyMessage(int instance, List<Host> membership, Map<Integer, Operation> decided,byte[] state) {
        super(MSG_ID);
        this.instance = instance;
        this.membership = membership;
        this.decided=decided;
        this.state=state;
    }

    @Override
    public short getId() {
        return super.getId();
    }


    public int getInstance() {
        return this.instance;
    }

    public List<Host> getMembership() {
        return this.membership;
    }

    public Map<Integer, Operation> getDecided() {
        return decided;
    }

    public void setDecided(Map<Integer, Operation> decided) {
        this.decided = decided;
    }

    public byte[] getState() {
        return state;
    }

    public void setState(byte[] state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PrepareMessage{" +
                "instance=" + instance +
                "membership=" + membership +
                '}';
    }

    public static ISerializer<NotifyMessage> serializer = new ISerializer<NotifyMessage>() {
        @Override
        public void serialize(NotifyMessage msg, ByteBuf out) throws IOException {
            out.writeInt(msg.instance);
            out.writeInt(msg.getMembership().size());
            for (Host host : msg.membership) {
                Host.serializer.serialize(host, out);
            }
            out.writeInt(msg.decided.size());
            for (Map.Entry<Integer, Operation> entry : msg.decided.entrySet()) {
                out.writeInt(entry.getKey());
                Operation.serializer.serialize(entry.getValue(),out);
            }
            out.writeInt(msg.getState().length);
            out.writeBytes(msg.getState());
        }

        @Override
        public NotifyMessage deserialize(ByteBuf in) throws IOException {
            int instance = in.readInt();
            int hostSize = in.readInt();
            List<Host> membership = new LinkedList<>();
            for (int i = 0; i < hostSize; i++) {
                Host host = Host.serializer.deserialize(in);
                membership.add(i, host);
            }
            int map_Size = in.readInt();
            Map<Integer, Operation> decided = new HashMap<>();
            for (int i=0;i<map_Size;i++){
                int decision_instance = in.readInt();
                Operation op = Operation.serializer.deserialize(in);
                decided.put(decision_instance,op);
            }
            byte[] state = new byte[in.readInt()];
            in.readBytes(state);
            return new NotifyMessage(instance, membership,decided,state);
        }
    };

}
