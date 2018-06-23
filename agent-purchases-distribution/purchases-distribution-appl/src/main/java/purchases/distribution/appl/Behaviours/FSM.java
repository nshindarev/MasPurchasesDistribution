package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class FSM {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public static class Wait extends WakerBehaviour {
        public Wait(Agent agent, long timeout){
            super(agent, timeout);
        }
    }

    public static class Quit extends OneShotBehaviour {
        @Override
        public void action(){}
    }
}
