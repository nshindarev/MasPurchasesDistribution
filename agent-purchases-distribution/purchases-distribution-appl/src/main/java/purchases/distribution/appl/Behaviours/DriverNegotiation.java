package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.Agent;
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
    protected void handleStateEntered(Behaviour behaviour){
        if(behaviour instanceof FSM.Wait){
            behaviour.reset();
        }
    }

    @Override
    public void onStart(){
        Behaviour        wait      = new FSM.Wait(myAgent, 2000);
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
                getDataStore().put("currently_considering", best);
                return best;
            }
        };
        GatherProposal   gather    = new GatherProposal(myAgent, "request-drop");
        AcceptProposal   accept    = new AcceptProposal(myAgent, "request-drop"){
            @Override
            public void onSuccess(Offer offer){
                ((DriverAgent) myAgent).changePickPoint((String)getDataStore().get("currently_considering"));
                ((Logger) getDataStore().get("logger")).info("offer " + offer);
                getDataStore().put("supply_chain", offer.supply_chain);
                getDataStore().put("found_mediator", true);
                Behaviour inform = new InformClients(myAgent);
                inform.setDataStore(getDataStore());
                myAgent.addBehaviour(inform);
            }
        };
        OneShotBehaviour removeBest = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                HashMap<String, Double> points = (HashMap<String, Double>) getDataStore().get("promising_points");
                String best = (String) getDataStore().get("currently_considering");
                points.remove(best);
            }
        };
        Behaviour        quit      = new FSM.Quit();
        LowerExpectations lower_expectations = new LowerExpectations(myAgent);

        broadcast.setDataStore(getDataStore());
        gather.setDataStore(getDataStore());
        accept.setDataStore(getDataStore());
        wait.setDataStore(getDataStore());
        removeBest.setDataStore(getDataStore());
        quit.setDataStore(getDataStore());
        lower_expectations.setDataStore(getDataStore());

        registerFirstState(wait, "wait");
        registerState(broadcast, "broadcast");
        registerState(gather, "gather");
        registerState(accept, "accept");
        registerState(removeBest, "remove_best");
        registerState(lower_expectations, "lower_expectations");
        registerLastState(quit, "quit");

        registerDefaultTransition("wait", "broadcast");

        registerTransition("broadcast", "gather",  FSM.SUCCESS);
        registerTransition("broadcast", "wait", FSM.FAILURE);

        registerTransition("gather", "accept", FSM.SUCCESS);
        registerTransition("gather", "lower_expectations", FSM.FAILURE);
        registerTransition("gather", "remove_best", 3);

        registerTransition("accept", "quit", FSM.SUCCESS);
        registerTransition("accept", "lower_expectations", FSM.FAILURE);


        registerDefaultTransition("remove_best", "wait");
        registerDefaultTransition("lower_expectations", "wait");
    }
}
