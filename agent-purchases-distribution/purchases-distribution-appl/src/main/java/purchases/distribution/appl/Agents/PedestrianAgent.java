package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChooseBest extends Behaviour {
    private int counter;
    private ACLMessage chosen_reply;
    private int best_offer = Integer.MAX_VALUE;

    private static final MessageTemplate template =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
            MessageTemplate.MatchReplyWith("initial-negotiation")
        );

    public ChooseBest(Agent agent, int expected_replies){
        super(agent);
        counter = expected_replies;
    }

    @Override
    public boolean done(){
        return counter == 0;
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            ACLMessage reply = msg.createReply();
            int offer = Integer.parseInt(msg.getContent());
            if(offer < best_offer){
                best_offer = offer;
                chosen_reply.setPerformative(ACLMessage.REFUSE);
                myAgent.send(reply);
                chosen_reply = reply;
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                myAgent.send(reply);
            }
            counter--;
            if(counter == 0){
                chosen_reply.setPerformative(ACLMessage.AGREE);
                myAgent.send(reply);
            }
        } else block();
    }
}

class PedestrianBehaviour extends OneShotBehaviour {
    private String address;

    public PedestrianBehaviour(Agent agent, String address){
        super(agent);
        this.address = address;
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
            msg.setReplyWith("initial-negotiation");
            msg.setContent(address);
            myAgent.send(msg);
            myAgent.addBehaviour(new ChooseBest(myAgent, agents.length));
        } catch(FIPAException ex){
            ex.printStackTrace();
        }
    }
}

public class PedestrianAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(PedestrianAgent.class);

    public String address;

    @Override
    public void setup() {
        addBehaviour(new PedestrianBehaviour(this, address));
    }
}
