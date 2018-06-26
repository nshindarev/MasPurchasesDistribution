package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import purchases.distribution.appl.Agents.DriverAgent;
import purchases.distribution.appl.Util.Offer;
import org.slf4j.Logger;

public class AcceptProposal extends Behaviour {
    public static final long TIMEOUT = 1000;

    private int state = 0;
    private int current_index = 0;
    private String topic;
    private boolean allDone = false;
    private boolean success = false;
    private boolean receiving = false;
    private boolean gotExpensive = false;
    private ArrayList<Offer> offers;

    private final MessageTemplate template;

    public AcceptProposal(Agent agent, String topic){
        super(agent);
        this.topic = topic;
        template =
            MessageTemplate.and(
                MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                ),
                MessageTemplate.MatchInReplyTo(topic)
            );
    }

    private void removeUnacceptable(){
        double acceptable_price = (double) getDataStore().get("acceptable_price");
        int i;
        for(i = 0; i < offers.size(); i++)
            if(offers.get(i).price > acceptable_price) break;
        offers.subList(i, offers.size()).clear();
    }

    public void onSuccess(Offer offer){}

    @Override
    public void onStart(){
        offers = (ArrayList<Offer>) getDataStore().get("acceptable_offers");
        offers.sort((Offer a, Offer b) -> a.price < b.price ? -1 : 1);
        removeUnacceptable();
        allDone = success = false;
    }

    @Override
    public void reset(){
        allDone = success = false;
        state = 0;
        current_index = 0;
        offers = null;
        gotExpensive = false;
    }

    @Override
    public int onEnd(){
        if(success){
            onSuccess(offers.get(0));
            ((Logger)getDataStore().get("logger")).info("YAY");
            reset();
            return FSM.SUCCESS;
        } else if(gotExpensive){
            reset();
            return FSM.FAILURE;
        } else {
            reset();
            return 3;
        }
    }

    @Override
    public boolean done(){
        return allDone;
    }

    @Override
    public void action(){
        offers = (ArrayList<Offer>) getDataStore().get("acceptable_offers");
        ACLMessage msg;
        removeUnacceptable();
        if(offers.isEmpty()){
            ((Logger)getDataStore().get("logger")).info("no offers");
            allDone = true;
            success = false;
            getDataStore().remove("currently_agreeing");
            return;
        }
        Offer offer = offers.get(0);
        switch(state){
        case 0: // отправить ACCEPT_PROPOSAL лучшему предложению
            getDataStore().put("currently_agreeing", true);
            msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            msg.setInReplyTo(topic);
            msg.setReplyWith(topic);
            msg.setConversationId(offer.convId);
            msg.addReceiver(offer.partner);
            myAgent.send(msg);
            ((Logger)getDataStore().get("logger")).info("sent an ACCEPT");
            state = 1;
            allDone = false;
            break;
        case 1: // ждать ответ, AGREE => успех, REFUSE => обновить цену предложения и state <- 0
            allDone = false;
            msg = myAgent.receive(
                MessageTemplate.and(
                    template,
                    MessageTemplate.MatchConversationId(offer.convId)
                )
            );
            if(msg != null){
                receiving = false;
                if(msg.getPerformative() == ACLMessage.AGREE){
                    ((Logger)getDataStore().get("logger")).info("YAY");
                    state = 2;
                } else {
                    ((Logger)getDataStore().get("logger")).info("OH NOES "+ msg.getContent());
                    if(msg.getContent().length() == 0){
                        offers.remove(0);
                    } else {
                        gotExpensive = true;
                        offer.price = Double.parseDouble(msg.getContent());
                        for(int i = current_index + 1; i < offers.size(); i++){
                            if(offers.get(i).price < offer.price){
                                offers.set(i - 1, offers.get(i));
                                offers.set(i, offer);
                            } else break;
                        }
                        removeUnacceptable();
                    }
                    state = 0;
                }
            } else block();
            break;
        case 2: // отправить остальным REJECT_PROPOSAL
            for(int i = 1; i < offers.size(); i++){
                msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                msg.setInReplyTo(topic);
                msg.setReplyWith(topic);
                msg.setConversationId(offers.get(i).convId);
                msg.addReceiver(offers.get(i).partner);
                myAgent.send(msg);
            }
            allDone = success = true;
            state = 0;
        }
    }
}
