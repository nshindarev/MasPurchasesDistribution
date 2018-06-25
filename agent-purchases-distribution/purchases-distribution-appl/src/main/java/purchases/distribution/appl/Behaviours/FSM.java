package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import org.slf4j.Logger;

public class FSM {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public static class Halt extends CyclicBehaviour {
        @Override
        public void action(){
            block();
        }
    }

    public static class Wait extends WakerBehaviour {
        public Wait(Agent agent, long timeout){
            super(agent, timeout);
        }

        @Override
        public void onStart(){
            ((Logger) getDataStore().get("logger")).info("starting to wait");
        }

        @Override
        public int onEnd(){
            ((Logger) getDataStore().get("logger")).info("done waiting");
            return SUCCESS;
        }
    }

    public static class Quit extends OneShotBehaviour {
        @Override
        public void action(){}
    }
}
