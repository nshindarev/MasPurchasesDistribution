package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import purchases.distribution.appl.Behaviours.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PedestrianBehaviour extends SequentialBehaviour {
    public PedestrianBehaviour(Agent agent, String address){
        super(agent);
        BroadcastCFP       broadcast = new BroadcastCFP(agent, "request-drop", address);
        ChooseBestProposal choose    = new ChooseBestProposal(agent, "request-drop");

        broadcast.setDataStore(getDataStore());
        choose.setDataStore(getDataStore());

        addSubBehaviour(broadcast);
        addSubBehaviour(choose);
    }
}

public class PedestrianAgent extends Agent {
    public static final Logger logger = LoggerFactory.getLogger(PedestrianAgent.class);

    @Override
    public void setup() {
        addBehaviour(new PedestrianBehaviour(this, (String)getArguments()[0]));
    }
}
