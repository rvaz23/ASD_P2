package protocols.agreement.timers;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class Timeout extends ProtoTimer {

    public static final short TIMEOUT_ID = 101;

    public Timeout() {
        super(TIMEOUT_ID);
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
