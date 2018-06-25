package purchases.distribution.appl.Behaviours;

import java.util.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import purchases.distribution.appl.Util.*;
import purchases.distribution.appl.Agents.DriverAgent;
import org.slf4j.Logger;

public class RouteAnalyzer extends CyclicBehaviour {
    private static final MessageTemplate template =
        MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchReplyWith("my-route")
        );

    private HashMap<String, Double> memory;
    private ArrayList<String> chain;

    public RouteAnalyzer(Agent agent){
        super(agent);
    }

    @Override
    public void onStart(){
        if(!getDataStore().containsKey("promising_points"))
            getDataStore().put("promising_points", new HashMap<String, Double>());

        memory = (HashMap<String, Double>) getDataStore().get("promising_points");
        chain = (ArrayList<String>) getDataStore().get("supply_chain");
    }

    @Override
    public void action(){
        ACLMessage msg = myAgent.receive(template);
        if(msg != null){
            if(getDataStore().containsKey("found_mediator")) return;
            if(chain.contains(msg.getSender().getLocalName())) return;
            Route route = ((DriverAgent)myAgent).getRoute();
            Route other = Route.fromString(msg.getContent());

            String point = route.optimalMiddlePoint(other);
            if(point == null) return;

            Route new_this = route.changePickPoint(point);
            Route new_that = other.addDropPoint(point);
            double profit = route.length() - new_this.length() + other.length() - new_that.length();
            ((Logger) getDataStore().get("logger")).info(point + ' ' + profit);
            if(!memory.containsKey(point) || memory.get(point) < profit)
                memory.put(point, profit);
        } else block();
    }
}
