package protocols.app.utils;

import org.apache.commons.lang3.tuple.Pair;

public class Tuple {

    private int seq;
    private int val;

    public Tuple(int seq, int val) {
        this.seq = seq;
        this.val = val;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }
}
