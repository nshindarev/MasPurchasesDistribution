package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Agents.*;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.Util.*;
import org.slf4j.Logger;

public class InformClients extends Behaviour {
    public InformClients(Agent agent){
        super(agent);
    }

    private Collector collector = null;

    @Override
    public boolean done(){
        if(collector != null && collector.done()){
            ((Logger)getDataStore().get("logger")).info("client collector is done");
            collector = null;
            return true;
        }
        HashSet<AID> clients = (HashSet<AID>) getDataStore().get("client_list");
        return clients.size() == 0;
    }

    @Override
    public void action(){
        if(collector != null) return;
        ArrayList<String> chain = (ArrayList<String>) getDataStore().get("supply_chain");
        HashSet<AID> clients = (HashSet<AID>) getDataStore().get("client_list");
        ((Logger)getDataStore().get("logger")).info("going to inform " + clients.size() + " clients");
        if(clients.size() == 0) return;
        StringBuilder content = new StringBuilder();
        for(String supplier : chain){
            content.append(supplier); content.append('\n');
        }
        for(AID partner : clients){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(partner);
            msg.setReplyWith("supply-chain");
            msg.setContent(content.toString());
            myAgent.send(msg);
        }
        collector = new Collector(myAgent, clients.size(), "ack");
        collector.setDataStore(getDataStore());
        myAgent.addBehaviour(collector);
    }

    @Override
    public int onEnd(){
        ((Logger)getDataStore().get("logger")).info("done informing");
        return 0;
    }
}
