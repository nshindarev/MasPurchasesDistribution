package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import org.slf4j.Logger;
import purchases.distribution.appl.Agents.*;


public class RoutePing extends SequentialBehaviour {
    public RoutePing(Agent agent){
        super(agent);
    }

    private Collector acks = null;

    @Override
    public void onStart(){
        Broadcast broadcast_route = new Broadcast(myAgent, ACLMessage.INFORM, "my-route"){
            @Override
            public String getContent(){
                String current = ((DriverAgent)myAgent).getRoute().toString();
                ((Logger)getDataStore().get("logger")).info("PING");
                return current;
            }
        };
        acks = new Collector(myAgent, ((int) getDataStore().get("total_drivers")) - 1, "ack");

        broadcast_route.setDataStore(getDataStore());
        acks.setDataStore(getDataStore());

        addSubBehaviour(broadcast_route);
        addSubBehaviour(acks);
    }

    @Override
    public int onEnd(){
        removeSubBehaviour(acks);
        ((Logger)getDataStore().get("logger")).info("routeping done: " + done());
        return FSM.SUCCESS;
    }
}
