package protocols.agreement;

import protocols.app.utils.Operation;
import pt.unl.fct.di.novasys.network.data.Host;

import java.util.UUID;

public class PaxosInstance {

    private final Operation operation;
    private byte[] proposer_value;
    private int proposer_seq;
    private int highest_prepare;
    private int highest_accepted;
    private byte[] highest_value;
    private int[] prepare_ok_set;
    private int[] accept_ok_set;
    private boolean decided;
    private Host[] all_processes;

    public PaxosInstance(byte[] proposer_value, int proposer_seq, Operation operation) {
        this.proposer_value = proposer_value;
        this.proposer_seq = proposer_seq;
        this.operation = operation;
    }

    public byte[] getProposer_value() {
        return proposer_value;
    }

    public void setProposer_value(byte[] proposer_value) {
        this.proposer_value = proposer_value;
    }

    public int getProposer_seq() {
        return proposer_seq;
    }

    public void setProposer_seq(int proposer_seq) {
        this.proposer_seq = proposer_seq;
    }

    public int getHighest_prepare() {
        return highest_prepare;
    }

    public void setHighest_prepare(int highest_prepare) {
        this.highest_prepare = highest_prepare;
    }

    public int getHighest_accepted() {
        return highest_accepted;
    }

    public void setHighest_accepted(int highest_accepted) {
        this.highest_accepted = highest_accepted;
    }

    public byte[] getHighest_value() {
        return highest_value;
    }

    public void setHighest_value(byte[] highest_value) {
        this.highest_value = highest_value;
    }

    public int[] getPrepare_ok_set() {
        return prepare_ok_set;
    }

    public void setPrepare_ok_set(int[] prepare_ok_set) {
        this.prepare_ok_set = prepare_ok_set;
    }

    public int[] getAccept_ok_set() {
        return accept_ok_set;
    }

    public void setAccept_ok_set(int[] accept_ok_set) {
        this.accept_ok_set = accept_ok_set;
    }

    public boolean isDecided() {
        return decided;
    }

    public void setDecided(boolean decided) {
        this.decided = decided;
    }

    public Host[] getAll_processes() {
        return all_processes;
    }

    public void setAll_processes(Host[] all_processes) {
        this.all_processes = all_processes;
    }
}
