package purchases.distribution.appl.Behaviours;

import java.util.HashMap;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.Offer;
import purchases.distribution.appl.Agents.DriverAgent;

public class CollectResponses extends CyclicBehaviour {
    private final MessageTemplate template;
    private HashMap<AID, Offer> memory;

    public CollectResponses(Agent agent, String topic){
        super(agent);
        template =
            MessageTemplate.and(
                MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
                ),
                MessageTemplate.MatchInReplyTo(topic)
            );
    }

    @Override
    public void onStart(){
        if(!getDataStore().containsKey("proposal_memory"))
            getDataStore().put("proposal_memory", new HashMap<AID, Offer>());

        memory = (HashMap<AID, Offer>) getDataStore().get("proposal_memory");
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                Offer offer = memory.get(msg.getSender());
                ((DriverAgent)myAgent).addImportantPoint(offer.newNode);
                //ACLMessage reply = msg.createReply();
                //if(offer != null){
                //    ((DriverAgent)myAgent).addImportantPoint(offer.newNode);
                //    reply.setPerformative(ACLMessage.AGREE);
                //} else {
                //    reply.setPerformative(ACLMessage.CANCEL);
                //}
                //myAgent.send(reply);
            }
        } else block();
    }
}
