package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class ChooseBestProposal extends Behaviour {
    private int counter;
    private ACLMessage chosen_reply;
    private double best_offer = Integer.MAX_VALUE;

    private final MessageTemplate template;

    public ChooseBestProposal(Agent agent, String topic){
        super(agent);
        template =
            MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                MessageTemplate.MatchInReplyTo(topic)
            );
    }

    @Override
    public void onStart(){
        counter = (int) getDataStore().get("num_agents");
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
            reply.setInReplyTo(msg.getInReplyTo());
            double offer = Double.parseDouble(msg.getContent());
            if(offer < best_offer){
                best_offer = offer;
                if(chosen_reply != null){
                    chosen_reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    myAgent.send(reply);
                }
                chosen_reply = reply;
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                myAgent.send(reply);
            }
            counter--;
            if(counter == 0){
                chosen_reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                myAgent.send(chosen_reply);
            }
        } else block();
    }
}
