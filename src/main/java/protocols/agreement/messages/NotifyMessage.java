package protocols.agreement.messages;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class NotifyMessage extends ProtoMessage {

    public static final short MSG_ID = 104;

    private int instance;
    private List<Host> membership;

    public NotifyMessage(int instance, List<Host> membership) {
        super(MSG_ID);
        this.instance = instance;
        this.membership = membership;
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
            return new NotifyMessage(instance, membership);
        }
    };

}
