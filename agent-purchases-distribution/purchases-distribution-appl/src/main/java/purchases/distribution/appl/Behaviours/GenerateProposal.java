package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.Request;
import purchases.distribution.appl.Agents.DriverAgent;
import org.slf4j.Logger;

public class GenerateProposal extends CyclicBehaviour {
    private final MessageTemplate template;
    private HashMap<AID, Request> memory;
    private ArrayList<String> chain;

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
        if(!getDataStore().containsKey("supply_chain"))
            getDataStore().put("supply_chain", new ArrayList<String>());
        chain = (ArrayList<String>) getDataStore().get("supply_chain");

        if(!getDataStore().containsKey("proposal_memory"))
            getDataStore().put("proposal_memory", new HashMap<AID, Request>());
        memory = (HashMap<AID, Request>) getDataStore().get("proposal_memory");
    }

    @Override
    public void action(){
        //if(getDataStore().containsKey("currently_agreeing")) return;
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            ACLMessage reply = msg.createReply();
            if(chain.contains(msg.getSender().getLocalName())){
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("");
                myAgent.send(reply);
                return;
            }
            reply.setPerformative(ACLMessage.PROPOSE);
            double cost = 100 * ((DriverAgent)myAgent).calculateDeviationCost(msg.getContent());
            cost = 50 + Math.max(cost, 0);
            StringBuilder content = new StringBuilder();
            content.append(Double.toString(cost)); content.append('\n');
            for(String supplier : chain){
                content.append(supplier); content.append('\n');
            }
            content.append(myAgent.getAID().getLocalName());
            ((Logger) getDataStore().get("logger")).info("sending proposal\n" + content.toString());
            reply.setContent(content.toString());
            myAgent.send(reply);
            memory.put(msg.getSender(), new Request(msg.getSender(), msg.getContent(), msg.getConversationId(), cost));
        } else block();
    }
}
