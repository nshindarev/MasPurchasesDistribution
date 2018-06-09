package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class BroadcastCFP extends OneShotBehaviour {
    private String topic, content;

    public BroadcastCFP(Agent agent, String topic, String content){
        super(agent);
        this.topic = topic;
        this.content = content;
    }

    @Override
    public void action(){
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("goods-distribution");
        template.addServices(sd);
        try {
            DFAgentDescription[] agents = DFService.search(myAgent, template);
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            for(int i = 0; i < agents.length; i++){
                msg.addReceiver(agents[i].getName());
            }
            msg.setReplyWith(topic);
            msg.setContent(content);
            myAgent.send(msg);
            getDataStore().put("num_agents", agents.length);
        } catch(FIPAException ex){
            ex.printStackTrace();
        }
    }
}
