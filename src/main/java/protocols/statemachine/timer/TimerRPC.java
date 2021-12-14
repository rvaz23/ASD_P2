package protocols.statemachine.timer;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class TimerRPC extends ProtoTimer {

    public static final short TIMEOUT_ID = 202;

    private int instance;

    public TimerRPC(Short id, int instance) {
        super(id);
        this.instance = instance;
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
