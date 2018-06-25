package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Agents.*;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.Util.*;
import org.slf4j.Logger;

public class InformClients extends OneShotBehaviour {
    public InformClients(Agent agent){
        super(agent);
    }

    @Override
    public void action(){
        ArrayList<String> chain = (ArrayList<String>) getDataStore().get("supply_chain");
        HashSet<AID> clients = (HashSet<AID>) getDataStore().get("client_list");
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
        getDataStore().remove("currently_agreeing");
    }
}
