package purchases.distribution.appl.Agents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Behaviours.*;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;
import purchases.distribution.appl.Util.DataPool;
import purchases.distribution.appl.Util.Offer;
import purchases.distribution.appl.Util.Route;
import purchases.distribution.appl.Util.Status;
import purchases.distribution.appl.Util.VertexStatus;

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
    public DriverBehaviour(Agent agent, Logger logger){
        super(agent, ParallelBehaviour.WHEN_ANY);
        GenerateProposal genprop = new GenerateProposal(agent, "request-drop");
        CollectResponses collect = new CollectResponses(agent, "request-drop");

        getDataStore().put("logger", logger);

        genprop.setDataStore(getDataStore());
        collect.setDataStore(getDataStore());

        addSubBehaviour(genprop);
        addSubBehaviour(collect);
        addSubBehaviour(new PrintRoute(agent, 10000));
    }
};

public class DriverAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(DriverAgent.class);

    public double calculateDeviationCost(String point){
        logger.info("someone wants to add " + point);
        return route.addDropPoint(point).length() - route.length();
    }

    public void addImportantPoint(String point){
        logger.info("adding point " + point);
        route = route.addDropPoint(point);
    }

    private Route route;
    private double init_length = 0;

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

        /**
         *
         *  НИЖЕ ИНИЦИАЛИЗАЦИЯ ИЗ CITIZEN AGENT
         *
         */

        logger.info("Agent " + getAID().getName() + " created");

        Object[] args = getArguments();
        if (args.length >= 1) {
            ArrayList<String> mains = new ArrayList<>();
            for(Object arg : args)
                mains.add((String)arg);
            mains.add(mains.get(0));
            route = new Route(mains, null, new HashSet<String>());
            init_length = route.length();
            logger.info("initial route: " + route.toString() + ' ' + init_length);
            addBehaviour(new DriverBehaviour(this, logger));
        }
        else {
            logger.error("args for agents " + getAID().getName() + " set incorrect");
            logger.error("agent " + getAID().getName() + " will be destroyed");
            this.doDelete();
        }
    }

    public void printWay(){
        logger.info(route.toString() + ' ' + route.length() + ' ' + init_length);
    }
}
