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

public abstract class Broadcast extends OneShotBehaviour {
    private String topic;
    private int performative;
    private int result = FSM.FAILURE;

    abstract public String getContent();

    public Broadcast(Agent agent, int performative, String topic){
        super(agent);
        this.performative = performative;
        this.topic = topic;
    }

    @Override
    public int onEnd(){
        return result;
    }

    @Override
    public void action(){
        result = FSM.FAILURE;
        ArrayList<String> convIds = new ArrayList<>();
        getDataStore().put("possible_partners", convIds);

        String content = getContent();
        if(content == null) return;

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("goods-distribution");
        template.addServices(sd);
        try {
            DFAgentDescription[] agents = DFService.search(myAgent, template);
            for(int i = 0; i < agents.length; i++){
                if(agents[i].getName().equals(myAgent.getAID())) continue;
                String convId = UUID.randomUUID().toString();
                ACLMessage msg = new ACLMessage(performative);
                msg.addReceiver(agents[i].getName());
                msg.setReplyWith(topic);
                msg.setContent(content);
                msg.setConversationId(convId);
                myAgent.send(msg);
                convIds.add(convId);
            }
            if(!convIds.isEmpty()){
                result = FSM.SUCCESS;
            }
        } catch(FIPAException ex){
            ex.printStackTrace();
        }
    }
}
