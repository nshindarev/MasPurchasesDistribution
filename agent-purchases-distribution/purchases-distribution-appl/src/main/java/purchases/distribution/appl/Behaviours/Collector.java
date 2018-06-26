package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Collector extends Behaviour {
    private int count;
    private double current;

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    private MessageTemplate template;

    public Collector(Agent agent, int count, String topic){
        super(agent);
        this.count = count;
        template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchReplyWith(topic)
        );
        logger.info("Waiting for " + count + " responses");
    }

    public void handle(ACLMessage msg){}

    @Override
    public boolean done(){
        logger.info("checking if done in collector: " + (count == 0));
        return count == 0;
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            handle(msg);
            count--;
            logger.info("got one from " + msg.getSender().getLocalName());
            logger.info("waiting for " + count + " responses");
        } else block();
    }
}
