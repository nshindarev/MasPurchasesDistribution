package purchases.distribution.appl.Behaviours;

import java.util.*;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import purchases.distribution.appl.Behaviours.*;

public class BroadcastCFP extends OneShotBehaviour {
    private String topic, content;
    private int result = FSM.FAILURE;

    public BroadcastCFP(Agent agent, String topic, String content){
        super(agent);
        this.topic = topic;
        this.content = content;
    }

    @Override
    public int onEnd(){
        return result;
    }

    @Override
    public void action(){
        result = FSM.FAILURE;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("goods-distribution");
        template.addServices(sd);
        try {
            DFAgentDescription[] agents = DFService.search(myAgent, template);
            ArrayList<String> convIds = new ArrayList<>();
            for(int i = 0; i < agents.length; i++){
                String convId = UUID.randomUUID().toString();
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.addReceiver(agents[i].getName());
                msg.setReplyWith(topic);
                msg.setContent(content);
                msg.setConversationId(convId);
                myAgent.send(msg);
                convIds.add(convId);
            }
            if(!convIds.isEmpty()){
                result = FSM.SUCCESS;
                getDataStore().put("possible_partners", convIds);
            }
        } catch(FIPAException ex){
            ex.printStackTrace();
        }
    }
}
