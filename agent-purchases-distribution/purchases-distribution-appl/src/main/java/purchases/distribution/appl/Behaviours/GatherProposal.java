package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.Offer;

public class GatherProposal extends ParallelBehaviour {
    public static final long TIMEOUT = 10000;
    private ReceiverBehaviour.Handle[] handles;
    private Logger logger;

    private final MessageTemplate template;

    public GatherProposal(Agent agent, String topic){
        super(agent, ParallelBehaviour.WHEN_ALL);
        template =
            MessageTemplate.and(
                MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
                ),
                MessageTemplate.MatchInReplyTo(topic)
            );
    }

    @Override
    public void onStart(){
        if(!getDataStore().containsKey("supply_chain"))
            getDataStore().put("supply_chain", new ArrayList<String>());
        ArrayList<String> ids = (ArrayList<String>) getDataStore().get("possible_partners");
        logger = (Logger) getDataStore().get("logger");
        logger.info("gathering offers");
        handles = new ReceiverBehaviour.Handle[ids.size()];
        for(int i = 0; i < ids.size(); i++){
            handles[i] = ReceiverBehaviour.newHandle();
            addSubBehaviour(new ReceiverBehaviour(myAgent, handles[i], TIMEOUT,
                MessageTemplate.and(
                    template,
                    MessageTemplate.MatchConversationId(ids.get(i))
                )
            ));
        }
    }

    @Override
    public int onEnd(){
        DataStore ds = getDataStore();
        ArrayList<Offer> offers = new ArrayList<>();
        double acceptable_price = (double) ds.get("acceptable_price");
        logger.info("got some offers");
        logger.info("acceptable price: " + acceptable_price);
        boolean hadOffers = false;
        ArrayList<String> chain = (ArrayList<String>) getDataStore().get("supply_chain");
        for(ReceiverBehaviour.Handle h : handles){
            try {
                ACLMessage msg = h.getMessage();
                if(msg.getPerformative() == ACLMessage.REFUSE) continue;
                if(chain.contains(msg.getSender().getLocalName())){
                    ACLMessage reply = msg.createReply();
                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    myAgent.send(reply);
                }
                hadOffers = true;
                String[] contentParts = msg.getContent().split("\\R", 2);
                double price = Double.parseDouble(contentParts[0]);
                logger.info("offer: " + price);
                logger.info("offer[1]: " + contentParts[1]);
                ArrayList<String> suppliers = new ArrayList<String>(Arrays.asList(contentParts[1].split("\\R")));
                logger.info("offer[2]: " + suppliers.toString());

                AID sender = msg.getSender();
                String convId = msg.getConversationId();
                if(price <= acceptable_price){
                    offers.add(new Offer(sender, price, convId, suppliers));
                } else {
                    ACLMessage reply = msg.createReply();
                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    myAgent.send(reply);
                }
            } catch(ReceiverBehaviour.TimedOut ex){
                continue;
            } catch(ReceiverBehaviour.NotYetReady ex){
                // не должно такого быть, TODO подумать
                continue;
            }
        }
        getDataStore().put("acceptable_offers", offers);
        if(!hadOffers) return 3;
        return offers.size() > 0 ? FSM.SUCCESS : FSM.FAILURE;
    }
}
