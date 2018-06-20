package purchases.distribution.appl.Behaviours;

import java.util.HashMap;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.Request;
import purchases.distribution.appl.Agents.DriverAgent;

public class CollectResponses extends CyclicBehaviour {
    private final MessageTemplate template;
    private HashMap<AID, Request> memory;

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
            getDataStore().put("proposal_memory", new HashMap<AID, Request>());

        memory = (HashMap<AID, Request>) getDataStore().get("proposal_memory");
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                DriverAgent driver = (DriverAgent) myAgent;
                Request request = memory.get(msg.getSender());
                ACLMessage reply = msg.createReply();
                if(request != null){
                    double price = driver.calculateDeviationCost(request.address);
                    if(price <= request.price){
                        driver.addImportantPoint(request.address);
                        reply.setPerformative(ACLMessage.AGREE);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(String.valueOf(price));
                    }
                } else {
                    reply.setPerformative(ACLMessage.CANCEL);
                }
                myAgent.send(reply);
            } else {
                memory.remove(msg.getSender());
            }
        } else block();
    }
}
