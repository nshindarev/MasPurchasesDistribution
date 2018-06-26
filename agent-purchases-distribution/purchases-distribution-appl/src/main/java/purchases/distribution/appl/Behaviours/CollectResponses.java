package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.*;
import purchases.distribution.appl.Agents.DriverAgent;

public class CollectResponses extends CyclicBehaviour {
    private final MessageTemplate template;
    private HashMap<AID, Request> memory;
    private ArrayList<String> chain;
    private HashSet<AID> clients;

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
        if(!getDataStore().containsKey("client_list"))
            getDataStore().put("client_list", new HashSet<AID>());
        clients = (HashSet<AID>) getDataStore().get("client_list");

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
            if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                DriverAgent driver = (DriverAgent) myAgent;
                ACLMessage reply = msg.createReply();
                ArrayList<Offer> offers = (ArrayList<Offer>) getDataStore().get("acceptable_offers");
                boolean contains = false;
                if(offers != null)
                    for(Offer offer : offers)
                        if(offer.partner.equals(msg.getSender())){
                            contains = true;
                            break;
                        }
                if(contains || chain.contains(msg.getSender().getLocalName())){
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("");
                    myAgent.send(reply);
                    return;
                }
                Request request = memory.get(msg.getSender());
                if(request != null){
                    double price = driver.calculateDeviationCost(request.address);
                    if(price <= request.price){
                        getDataStore().put("money", (double) getDataStore().get("money") + request.price);
                        memory.remove(request.partner);
                        driver.addImportantPoint(request.address);
                        reply.setPerformative(ACLMessage.AGREE);
                        if(request.partner.getLocalName().substring(0, 6).equals("Driver"))
                            clients.add(request.partner);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(String.valueOf(price));
                    }
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                myAgent.send(reply);
            } else {
                memory.remove(msg.getSender());
            }
        } else block();
    }
}
