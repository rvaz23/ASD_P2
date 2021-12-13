package protocols.app.utils;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

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

}
