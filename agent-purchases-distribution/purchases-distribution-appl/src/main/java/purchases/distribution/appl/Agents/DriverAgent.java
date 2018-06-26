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

class TimeLimit extends WakerBehaviour {
    public TimeLimit(Agent agent, long timeout){
        super(agent, timeout);
    }

    @Override
    public void handleElapsedTimeout(){
        ((Logger)getDataStore().get("logger")).info(((ArrayList<String>)getDataStore().get("supply_chain")).toString());
        ((DriverAgent) myAgent).printWay();
        ((DriverAgent) myAgent).reportDeviation();
        myAgent.doDelete();
    }
}


class UpdateChain extends CyclicBehaviour {
    private static final MessageTemplate template =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchReplyWith("supply-chain")
        );

    public UpdateChain(Agent agent){
        super(agent);
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            ArrayList<String> chain = (ArrayList<String>) getDataStore().get("supply_chain");
            for(String supplier : msg.getContent().split("\\R"))
                if(!chain.contains(supplier)) chain.add(supplier);
            ((Logger) getDataStore().get("logger")).info("current_chain " + chain.toString());
            SequentialBehaviour notify_clients = new SequentialBehaviour(myAgent);

            Behaviour inform = new InformClients(myAgent);
            inform.setDataStore(getDataStore());

            final ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setReplyWith("ack");

            Behaviour send_ack = new OneShotBehaviour(myAgent){
                @Override
                public void action(){
                    myAgent.send(reply);
                }
            };

            notify_clients.addSubBehaviour(inform);
            notify_clients.addSubBehaviour(send_ack);
            myAgent.addBehaviour(notify_clients);
        } else block();
    }
}

class PhaseTwo extends SequentialBehaviour {
    private static final MessageTemplate template =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.and(
                MessageTemplate.MatchSender(new AID("god", false)),
                MessageTemplate.MatchReplyWith("phase-two")
            )
        );

    public PhaseTwo(Agent agent){
        super(agent);
    }

    @Override
    public void onStart(){
        DataStore ds = getDataStore();
        Behaviour wait = new Behaviour(myAgent){
            private boolean over = false;
            @Override
            public void action(){
                ACLMessage msg = myAgent.receive(template);
                if(msg != null){
                    over = true;
                    ((Logger) getDataStore().get("logger")).info("total_drivers: " + Integer.parseInt(msg.getContent()));
                    getDataStore().put("total_drivers", Integer.parseInt(msg.getContent()));
                } else block();
            }
            @Override
            public boolean done(){
                return over;
            }
        };
        Behaviour addBehaviours = new OneShotBehaviour(myAgent){
            @Override
            public void action(){
                ((DriverAgent) myAgent).printWay();
                ((DriverAgent) myAgent).reportPDeviation();

                RouteAnalyzer    analyze = new RouteAnalyzer(myAgent);
                UpdateChain      update  = new UpdateChain(myAgent);
                RoutePing        ping = new RoutePing(myAgent);
                DriverNegotiation negotiation = new DriverNegotiation(myAgent);
                SequentialBehaviour seq = new SequentialBehaviour(myAgent);

                analyze.setDataStore(getDataStore());
                update.setDataStore(getDataStore());
                ping.setDataStore(getDataStore());
                negotiation.setDataStore(getDataStore());


                seq.addSubBehaviour(ping);
                seq.addSubBehaviour(negotiation);

                myAgent.addBehaviour(analyze);
                myAgent.addBehaviour(update);
                myAgent.addBehaviour(seq);
            }
        };

        wait.setDataStore(getDataStore());
        addBehaviours.setDataStore(getDataStore());

        addSubBehaviour(wait);
        addSubBehaviour(addBehaviours);
    }
}

class DriverBehaviour extends OneShotBehaviour {
    public DriverBehaviour(Agent agent, Logger logger){
        super(agent);
        getDataStore().put("logger", logger);
        getDataStore().put("money", 0.0);
        getDataStore().put("promising_points", new HashMap<String, Double>());
        getDataStore().put("acceptable_price", 50.0);
        getDataStore().put("supply_chain", new ArrayList<String>());
    }

    @Override
    public void action(){
        GenerateProposal genprop = new GenerateProposal(myAgent, "request-drop");
        CollectResponses collect = new CollectResponses(myAgent, "request-drop");
        PhaseTwo         phase   = new PhaseTwo(myAgent);


        genprop.setDataStore(getDataStore());
        collect.setDataStore(getDataStore());
        phase.setDataStore(getDataStore());

        myAgent.addBehaviour(genprop);
        myAgent.addBehaviour(collect);
        myAgent.addBehaviour(phase);
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

    public void changePickPoint(String point){
        logger.info("changing pick up point to " + point);
        route = route.changePickPoint(point);
    }

    private Route route;
    private double init_length = 0;
    private double ware_length = 0;
    private double pedestrian_length = 0;

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

        logger.info("Agent " + getAID().getName() + " created");

        Object[] args = getArguments();
        if (args.length >= 1) {
            ArrayList<String> mains = new ArrayList<>();
            for(Object arg : args)
                mains.add((String)arg);
            mains.add(mains.get(0));
            route = new Route(mains, null, new HashSet<String>());
            init_length = route.length();
            route = new Route(mains, DataPool.getStorageName(), new HashSet<String>());
            ware_length = route.length();
            logger.info("initial route: " + route.toString() + ' ' + ware_length);
            addBehaviour(new DriverBehaviour(this, logger));
        }
        else {
            logger.error("args for agents " + getAID().getName() + " set incorrect");
            logger.error("agent " + getAID().getName() + " will be destroyed");
            this.doDelete();
        }
    }

    public void printWay(){
        logger.info(route.toString() + ' ' + route.length() + ' ' + pedestrian_length + ' ' + ware_length + ' ' + init_length);
    }

    public void reportDeviation(){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setReplyWith("deviation");
        msg.addReceiver(new AID("god", false));
        msg.setContent("" + (route.length() - pedestrian_length));
        send(msg);
    }
    public void reportPDeviation(){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setReplyWith("pedestrian-deviation");
        msg.addReceiver(new AID("god", false));
        pedestrian_length = route.length();
        msg.setContent("" + pedestrian_length);
        send(msg);
    }

    public Route getRoute(){ return route; }
}
