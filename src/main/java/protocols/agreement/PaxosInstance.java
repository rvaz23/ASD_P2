package protocols.agreement;

import org.apache.commons.lang3.tuple.Pair;
import protocols.app.utils.Operation;
import protocols.app.utils.Tuple;
import pt.unl.fct.di.novasys.network.data.Host;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PaxosInstance {

    //private final Operation operation;
    private Operation proposer_value;
    private int proposer_seq;
    private int highest_prepare;
    private int highest_accepted;
    private Operation highest_value;
    private List<Tuple> prepare_ok_set;
    private List<Tuple> accept_ok_set;
    private Operation decided;
    private List<Host> all_processes;

    public PaxosInstance(Operation proposer_value, int proposer_seq,List<Host> membership) {
        this.proposer_value = proposer_value;
        this.proposer_seq = proposer_seq;
        this.all_processes=membership;
        prepare_ok_set= new LinkedList<Tuple>();
        accept_ok_set= new LinkedList<Tuple>();

    }

    public Operation getProposer_value() {
        return proposer_value;
    }

    public void setProposer_value(Operation proposer_value) {
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

    public Operation getHighest_value() {
        return highest_value;
    }

    public void setHighest_value(Operation highest_value) {
        this.highest_value = highest_value;
    }

    public List<Tuple> getPrepare_ok_set() {
        return prepare_ok_set;
    }

    public void add_prepare_ok(int na,Operation va){
        Tuple pair = new Tuple(na,va);
        prepare_ok_set.add(pair);
    }

    public void setPrepare_ok_set(List<Tuple> prepare_ok_set) {
        this.prepare_ok_set = prepare_ok_set;
    }

    public List<Tuple> getAccept_ok_set() {
        return accept_ok_set;
    }

    public void add_accept_ok(Tuple element){
        accept_ok_set.add(element);
    }

    public void setAccept_ok_set(List<Tuple> accept_ok_set) {
        this.accept_ok_set = accept_ok_set;
    }

    public Operation getDecided() {
        return decided;
    }

    public void setDecided(Operation decided) {
        this.decided = decided;
    }

    public List<Host> getAll_processes() {
        return all_processes;
    }

    public void setAll_processes(List<Host> all_processes) {
        this.all_processes = all_processes;
    }
}
