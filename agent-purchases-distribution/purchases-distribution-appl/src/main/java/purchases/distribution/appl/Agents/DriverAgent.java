package purchases.distribution.appl.Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.RouteHandler;
import purchases.distribution.appl.Util.Route;
import purchases.distribution.appl.Util.Status;
import purchases.distribution.appl.Util.VertexStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

class PrintRoute extends WakerBehaviour {
    public PrintRoute(Agent agent, long timeout){
        super(agent, timeout);
    }

    @Override
    protected void handleElapsedTimeout(){
        ((DriverAgent) myAgent).printWay();
    }
}

class DriverBehaviour extends ParallelBehaviour {
    public DriverBehaviour(Agent agent){
        super(agent, ParallelBehaviour.WHEN_ANY);
        GenerateProposal genprop = new GenerateProposal(agent, "request-drop");
        CollectResponses collect = new CollectResponses(agent, "request-drop");

        genprop.setDataStore(getDataStore());
        collect.setDataStore(getDataStore());

        addSubBehaviour(genprop);
        addSubBehaviour(collect);
        addSubBehaviour(new PrintRoute(agent, 10000));
    }
};

public class DriverAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(DriverAgent.class);

    public double calculateDeviationCost(String point){
        logger.info("someone wants to add " + point);
        return route.addDropPoint(point).length() - route.length();
    }

    public void addImportantPoint(String point){
        logger.info("adding point " + point);
        route = route.addDropPoint(point);
    }

    private Route route;
    private RouteHandler routeHandler;

    @Override
    public void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sds = new ServiceDescription();
        sds.setType("goods-distribution");
        sds.setName(getLocalName() + " express");
        dfd.addServices(sds);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException ex){
            ex.printStackTrace();
        }

        addBehaviour(new DriverBehaviour(this));

        /**
         *
         *  НИЖЕ ИНИЦИАЛИЗАЦИЯ ИЗ CITIZEN AGENT
         *
         */

        logger.info("Agent " + getAID().getName() + " created");

        Object[] args = getArguments();
        ArrayList<String> mains = new ArrayList<>();
        for(Object arg : args)
            mains.add((String)arg);
        mains.add(mains.get(0));
        route = new Route(mains, null, new HashSet<String>());
        logger.info("initial route: " + route.toString());
        if (args.length >= 1) {
            for(Object obj: args){
                this.routeHandler.ownWay.add(new VertexStatus((String)obj, Status.MAIN));
            }

            this.routeHandler.curWay = new LinkedList<>(this.routeHandler.ownWay);
            this.routeHandler.checkCyclicWays();
        }
        else {
            logger.error("args for agents " + getAID().getName() + " set incorrect");
            logger.error("agent " + getAID().getName() + " will be destroyed");
            this.doDelete();
        }
    }

    public void printWay(){
        logger.info(route.toString());
    }
}
