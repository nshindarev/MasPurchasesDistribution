package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import purchases.distribution.appl.Behaviours.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PedestrianBehaviour extends FSMBehaviour {

    public PedestrianBehaviour(Agent agent, String address, Logger logger){
        super(agent);
        BroadcastCFP     broadcast = new BroadcastCFP(agent, "request-drop", address);
        GatherProposal   gather    = new GatherProposal(agent, "request-drop");
        AcceptProposal   accept    = new AcceptProposal(agent, "request-drop");
        WakerBehaviour   wait      = new FSM.Wait(agent, 1000);
        OneShotBehaviour quit      = new FSM.Quit();
        OneShotBehaviour lower_expectations = new OneShotBehaviour(agent){
            @Override
            public void action(){
                DataStore ds = getDataStore();
                double price = (double) ds.get("acceptable_price");
                ds.put("acceptable_price", price + 100);
            }
        };

        getDataStore().put("acceptable_price", 50.0);
        getDataStore().put("logger", logger);

        broadcast.setDataStore(getDataStore());
        gather.setDataStore(getDataStore());
        accept.setDataStore(getDataStore());
        wait.setDataStore(getDataStore());
        quit.setDataStore(getDataStore());
        lower_expectations.setDataStore(getDataStore());

        registerFirstState(broadcast, "broadcast");
        registerState(gather, "gather");
        registerState(accept, "accept");
        registerState(wait, "wait");
        registerState(lower_expectations, "lower_expectations");
        registerLastState(quit, "quit");

        registerTransition("broadcast", "gather",  FSM.SUCCESS);
        registerTransition("broadcast", "wait", FSM.FAILURE);

        registerTransition("gather", "accept", FSM.SUCCESS);
        registerTransition("gather", "lower_expectations", FSM.FAILURE);

        registerTransition("accept", "quit", FSM.SUCCESS);
        registerTransition("accept", "lower_expectations", FSM.FAILURE);


        registerDefaultTransition("lower_expectations", "wait");
        registerDefaultTransition("wait", "broadcast");
    }
}

public class PedestrianAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(PedestrianAgent.class);

    @Override
    public void setup() {
        addBehaviour(new PedestrianBehaviour(this, (String)getArguments()[0], logger));
    }
}
