package protocols.agreement.multipaxos;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protocols.app.utils.Operation;
import protocols.statemachine.messages.RPCMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class OperationLog {

    // We use a sorted map so the key, a timestamp, are ordered among themselves
    private SortedMap<Long, Operation> log;

    public OperationLog() {
        log = new TreeMap<>();
    }

    public OperationLog(Map<Long, Operation> previous) {
        log = new TreeMap<>();
        log.putAll(previous);
    }

    public OperationLog(Operation op){
        log = new TreeMap<>();
        log.put(System.currentTimeMillis(), op);
    }

    public SortedMap<Long, Operation> getLog() {
        return log;
    }

    // Merge logs by concatenating the entries that come after the las timestamp present
    public void mergeLogs(SortedMap<Long, Operation> otherLog) {
        if (log.isEmpty()) {
            log.putAll(otherLog);
            return;
        }
        // Adds all entries with a key (timestamp) larger than the largest currently on the log
        log.putAll(otherLog.tailMap(log.lastKey()));
    }

    public Operation getLastOperation() {
        return log.get(log.lastKey());
    }

    int compare(OperationLog other) {
        if (this.log.isEmpty() && other.getLog().isEmpty())
            return 0;
        if (this.log.isEmpty())
            return -1;
        if (other.getLog().isEmpty())
            return 1;
        return this.log.lastKey().compareTo(other.getLog().lastKey());
    }

    public static ISerializer<OperationLog> serializer = new ISerializer<OperationLog>() {

        @Override
        public void serialize(OperationLog log, ByteBuf out) {
            out.writeInt(log.getLog().size());

            log.getLog().forEach((k, v) -> {
                out.writeLong(k);
                try {
                    Operation.serializer.serialize(v, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public OperationLog deserialize(ByteBuf in) throws IOException {
            int n = in.readInt();
            TreeMap<Long, Operation> t = new TreeMap<>();
            for (int i = 0; i < n; i++) {
                long key = in.readLong();
                Operation op = Operation.serializer.deserialize(in);
                t.put(key, op);
            }
            return new OperationLog(t);
        }
    };

}
