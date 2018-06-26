package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Agents.*;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.Util.*;
import org.slf4j.Logger;

public class DriverNegotiation extends FSMBehaviour {

    public DriverNegotiation(Agent agent){
        super(agent);
    }

    @Override
    public void onStart(){
        Behaviour        waitTurn  = new Behaviour(myAgent){
            private final MessageTemplate template =
                MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchReplyWith("your-turn")
                );
            private int result = FSM.SUCCESS;
            private boolean over = false;
            @Override
            public boolean done(){ return over; }
            @Override
            public void action(){
                ((Logger)getDataStore().get("logger")).info("waiting for my turn");
                ACLMessage msg = myAgent.receive(template);
                if(msg != null){
                    over = true;
                    getDataStore().put("proposal_memory", new HashMap<AID, Request>());
                    int timer = Integer.parseInt(msg.getContent());
                    getDataStore().put("timer", timer);
                    ((Logger)getDataStore().get("logger")).info("timer is now " + timer);
                    if(timer > 50 || getDataStore().containsKey("found_mediator"))
                        result = FSM.FAILURE;
                } else block();
            }
            @Override
            public int onEnd(){
                over = false;
                return result;
            }
        };
        Broadcast        broadcast = new Broadcast(myAgent, ACLMessage.CFP, "request-drop"){
            @Override
            public String getContent(){
                HashMap<String, Double> points = (HashMap<String, Double>) getDataStore().get("promising_points");
                String best = null;
                double max = 0;
                for(String point : points.keySet()){
                    double profit = points.get(point);
                    if(profit > max){
                        best = point;
                        max = profit;
                    }
                }
                ((Logger)getDataStore().get("logger")).info("broadcasting " + best);
                getDataStore().put("currently_considering", best);
                return best;
            }
        };
        GatherProposal   gather    = new GatherProposal(myAgent, "request-drop");
        AcceptProposal   accept    = new AcceptProposal(myAgent, "request-drop"){
            @Override
            public void onSuccess(Offer offer){
                ((DriverAgent) myAgent).changePickPoint((String)getDataStore().get("currently_considering"));
                ArrayList<String> chain = (ArrayList<String>) getDataStore().get("supply_chain");
                chain.clear();
                for(String supplier : offer.supply_chain){
                    chain.add(supplier);
                }
                ((Logger)getDataStore().get("logger")).info("supply_chain " + chain.toString());
                //getDataStore().put("supply_chain", offer.supply_chain);
                getDataStore().put("found_mediator", true);
            }
        };
        RoutePing        ping = new RoutePing(myAgent);
        InformClients    inform = new InformClients(myAgent);
        OneShotBehaviour advanceTimer = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                DataStore ds = getDataStore();
                int timer = (int) ds.get("timer");
                ds.put("timer", timer + 1);
                ((Logger)getDataStore().get("logger")).info("timer is now " + (timer + 1));
            }
        };
        OneShotBehaviour resetTimer = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                ((Logger)getDataStore().get("logger")).info("resetting timer");
                getDataStore().put("timer", 0);
            }
        };
        OneShotBehaviour sendNext = new OneShotBehaviour(myAgent){
            private int result = FSM.SUCCESS;
            @Override
            public void action(){
                DataStore ds = getDataStore();
                int timer = (int) ds.get("timer");
                int num = (int) ds.get("total_drivers");
                int cur = Integer.parseInt(myAgent.getAID().getLocalName().substring(6));
                int next = cur % num + 1;
                AID target = new AID("Driver" + next, false);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setReplyWith("your-turn");
                msg.addReceiver(target);
                msg.setContent("" + timer);
                myAgent.send(msg);
                ((Logger)getDataStore().get("logger")).info("passing turn to " + target.getLocalName());
                if(timer > 50)
                    result = FSM.FAILURE;
            }
            @Override
            public int onEnd(){
                return result;
            }
        };
        OneShotBehaviour lowerExp = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                DataStore ds = getDataStore();
                ((Logger) getDataStore().get("logger")).info("UPPING THE PRICE");
                //double price = Math.min((double) ds.get("acceptable_price") + 100, (double) ds.get("money"));
                double price = ((double) ds.get("acceptable_price")) + 100;
                ds.put("acceptable_price", price);
            }
        };
        OneShotBehaviour removeBest = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                ((Logger)getDataStore().get("logger")).info("removing best");
                HashMap<String, Double> points = (HashMap<String, Double>) getDataStore().get("promising_points");
                String best = (String) getDataStore().get("currently_considering");
                points.remove(best);
            }
        };
        Behaviour quit = new FSM.Quit();

        waitTurn.setDataStore(getDataStore());
        ping.setDataStore(getDataStore());
        inform.setDataStore(getDataStore());
        advanceTimer.setDataStore(getDataStore());
        resetTimer.setDataStore(getDataStore());
        sendNext.setDataStore(getDataStore());
        lowerExp.setDataStore(getDataStore());
        removeBest.setDataStore(getDataStore());
        broadcast.setDataStore(getDataStore());
        gather.setDataStore(getDataStore());
        accept.setDataStore(getDataStore());
        quit.setDataStore(getDataStore());

        if(myAgent.getAID().getLocalName().equals("Driver1")){
            registerFirstState(broadcast, "broadcast");
            registerState(waitTurn, "wait");
        } else {
            registerFirstState(waitTurn, "wait");
            registerState(broadcast, "broadcast");
        }
        registerState(gather, "gather");
        registerState(accept, "accept");
        registerState(removeBest, "remove_best");
        registerState(ping, "ping");
        registerState(inform, "inform");
        registerState(sendNext, "send_next");
        registerState(advanceTimer, "advance_timer");
        registerState(resetTimer, "reset_timer");
        registerState(lowerExp, "lower_expectations");
        registerLastState(quit, "quit");



        registerTransition("broadcast", "gather",  FSM.SUCCESS);
        registerTransition("broadcast", "advance_timer", FSM.FAILURE);

        registerTransition("gather", "accept", FSM.SUCCESS);
        registerTransition("gather", "lower_expectations", FSM.FAILURE);
        registerTransition("gather", "remove_best", 3);

        registerTransition("accept", "ping", FSM.SUCCESS);
        registerTransition("accept", "lower_expectations", FSM.FAILURE);

        registerTransition("send_next", "wait", FSM.SUCCESS);
        registerTransition("send_next", "quit", FSM.FAILURE);

        registerTransition("wait", "broadcast", FSM.SUCCESS);
        registerTransition("wait", "advance_timer", FSM.FAILURE);

        registerDefaultTransition("lower_expectations", "reset_timer");
        registerDefaultTransition("ping", "inform");
        registerDefaultTransition("inform", "reset_timer");
        registerDefaultTransition("remove_best", "advance_timer");
        registerDefaultTransition("advance_timer", "send_next");
        registerDefaultTransition("reset_timer", "send_next");

    }

    @Override
    public int onEnd(){
        ((Logger)getDataStore().get("logger")).info(((ArrayList<String>)getDataStore().get("supply_chain")).toString());
        ((DriverAgent)myAgent).printWay();
        ((DriverAgent)myAgent).reportDeviation();
        return 0;
    }
}
