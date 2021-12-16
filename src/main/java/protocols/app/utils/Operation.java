package protocols.app.utils;

import io.netty.buffer.ByteBuf;
import org.apache.commons.codec.binary.Hex;
import protocols.agreement.messages.NotifyMessage;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Operation {

    public final static byte NORMAL=(byte) 1;
    public final static byte ADD=(byte) 2;
    public final static byte REMOVE=(byte) 3;



    private final byte opType;
    private final String key;
    private final byte[] data;

    public Operation(byte opType, String key, byte[] data){
        this.opType = opType;
        this.key = key;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public byte getOpType() {
        return opType;
    }

    public String getKey() {
        new ByteArrayOutputStream().write(10);
        return key;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeByte(opType);
        dos.writeUTF(key);
        dos.writeInt(data.length);
        dos.write(data);
        return baos.toByteArray();
    }

    public static Operation fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        byte opType = dis.readByte();
        String key = dis.readUTF();
        byte[] opData = new byte[dis.readInt()];
        dis.read(opData, 0, opData.length);
        return new Operation(opType, key, opData);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "opType=" + opType +
                ", key='" + key + '\'' +
                ", data=" + Hex.encodeHexString(data) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return opType == operation.opType && key.equals(operation.key) && Arrays.equals(data, operation.data);
    }

    public static ISerializer<Operation> serializer = new ISerializer<Operation>() {
        @Override
        public void serialize(Operation op, ByteBuf out) throws IOException {
            out.writeByte(op.getOpType());
            out.writeInt(op.getKey().length());
            out.writeBytes(op.getKey().getBytes(StandardCharsets.UTF_8));
            out.writeInt(op.getData().length);
            out.writeBytes(op.getData());
        }

        @Override
        public Operation deserialize(ByteBuf in) throws IOException {
            byte opType = in.readByte();
            //int StringSize = in.readInt();
            byte[] aux = new byte[in.readInt()];
            in.readBytes(aux);
            String key = new String(aux,StandardCharsets.UTF_8);
            aux = new byte[in.readInt()];
            in.readBytes(aux);
            Operation op = new Operation(opType,key,aux);
            return op;
        }
    };

}
