package protocols.app.utils;

import org.apache.commons.lang3.tuple.Pair;

public class Tuple {

    private int seq;
    private Operation val;

    public Tuple(int seq, Operation op) {
        this.seq = seq;
        this.val = op;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Operation getVal() {
        return val;
    }

    public void setVal(Operation val) {
        this.val = val;
    }
}
