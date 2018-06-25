package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.Util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PedestrianBehaviour extends FSMBehaviour {

    public PedestrianBehaviour(Agent agent, String address, Logger logger){
        super(agent);
        Broadcast        broadcast = new Broadcast(agent, ACLMessage.CFP, "request-drop"){
            @Override
            public String getContent(){
                return address;
            }
        };
        GatherProposal   gather    = new GatherProposal(agent, "request-drop");
        AcceptProposal   accept    = new AcceptProposal(agent, "request-drop"){
            @Override
            public void onSuccess(Offer offer){
                logger.info("DONE");
                myAgent.doDelete();
            }
        };
        Behaviour        wait      = new FSM.Wait(agent, 1000);
        OneShotBehaviour quit      = new FSM.Quit();
        LowerExpectations lower_expectations = new LowerExpectations(agent);

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
