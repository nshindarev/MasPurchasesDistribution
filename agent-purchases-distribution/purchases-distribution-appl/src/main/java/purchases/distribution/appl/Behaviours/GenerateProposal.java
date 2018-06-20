package purchases.distribution.appl.Behaviours;

import java.util.HashMap;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.Request;
import purchases.distribution.appl.Agents.DriverAgent;

public class GenerateProposal extends CyclicBehaviour {
    private final MessageTemplate template;
    private HashMap<AID, Request> memory;

    public GenerateProposal(Agent agent, String topic){
        super(agent);
        template =
            MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchReplyWith(topic)
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
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            double cost = 100 * ((DriverAgent)myAgent).calculateDeviationCost(msg.getContent());
            cost = 50 + Math.max(cost, 0);
            reply.setContent(Double.toString(cost));
            myAgent.send(reply);
            memory.put(msg.getSender(), new Request(msg.getSender(), msg.getContent(), msg.getConversationId(), cost));
        } else block();
    }
}
