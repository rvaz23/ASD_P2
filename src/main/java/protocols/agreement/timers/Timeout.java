package protocols.agreement.timers;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class Timeout extends ProtoTimer {

    public static final short TIMEOUT_ID = 201;

    private int instance;

    public Timeout(Short id,int instance) {
        super(id);
        this.instance=instance;
    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    @Override
    public short getId() {
        return super.getId();
    }

    @Override
    public ProtoTimer clone() {
        return null;
    }
}
