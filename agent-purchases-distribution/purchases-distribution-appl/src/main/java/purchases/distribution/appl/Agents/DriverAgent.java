package purchases.distribution.appl.Agents;

import java.util.HashMap;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DriverPedestrianBehaviour extends ParallelBehaviour {
    private static final MessageTemplate cfpTemplate =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            MessageTemplate.MatchReplyWith("initial-negotiation")
        );

    private static final MessageTemplate cntTemplate =
        MessageTemplate.and(
            MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
            ),
            MessageTemplate.MatchReplyWith("initial-negotiation")
        );

    private HashMap<AID, String> memory = new HashMap<AID, String>();

    private class Proposer extends CyclicBehaviour {
        public Proposer(Agent agent){
            super(agent);
        }

        @Override
        public void action(){
            ACLMessage msg = myAgent.receive(cfpTemplate);
            if(msg != null){
                ACLMessage reply = msg.createReply();
                reply.setContent(
                    Integer.toString(
                        ((DriverAgent)myAgent).calculateDeviationCost(msg.getContent())
                    )
                );
                memory.put(msg.getSender(), msg.getContent());
                myAgent.send(reply);
            } else block();
        }
    }

    private class Counter extends CyclicBehaviour {
        public Counter(Agent agent){
            super(agent);
        }

        @Override
        public void action(){
            ACLMessage msg = myAgent.receive(cntTemplate);
            if(msg != null){
                if(msg.getPerformative() == ACLMessage.AGREE)
                    ((DriverAgent)myAgent).addImportantPoint(memory.get(msg.getSender()));
            } else block();
        }
    }

    public DriverPedestrianBehaviour(DriverAgent agent){
        super(agent, ParallelBehaviour.WHEN_ANY);
        addSubBehaviour(new Proposer(agent));
        addSubBehaviour(new Counter(agent));
    }
}

public class DriverAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(DriverAgent.class);

    public int calculateDeviationCost(String point){
        return 4;
    }

    public void addImportantPoint(String point){
    }

    @Override
    public void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sds = new ServiceDescription();
        sds.setType("goods-distribution");
        sds.setName(getLocalName() + " express");
        dfd.addServices(sds);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException ex){
            ex.printStackTrace();
        }

        addBehaviour(new DriverPedestrianBehaviour(this));
    }
}
