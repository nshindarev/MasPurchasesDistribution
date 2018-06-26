package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Collector extends Behaviour {
    private int count;
    private double current;

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    private final static MessageTemplate template =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchReplyWith("deviation")
        );

    public Collector(Agent agent, int count){
        super(agent);
        this.count = count;
        logger.info("Waiting for " + count + " responses");
    }

    @Override
    public boolean done(){
        return count == 0;
    }

    @Override
    public int onEnd(){
        logger.info("Total deviation: " + current);
        return 0;
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            current += Double.parseDouble(msg.getContent());
            logger.info("got one from " + msg.getSender().getLocalName());
            logger.info("waiting for " + count + " responses");
            count--;
        } else block();
    }
}

public class StatCollector extends Agent {
    @Override
    public void setup(){
        addBehaviour(new Collector(this, (int)(getArguments()[0])));
    }
}
