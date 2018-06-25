package purchases.distribution.appl.Behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import org.slf4j.Logger;

public class LowerExpectations extends OneShotBehaviour {
    public LowerExpectations(Agent agent){
        super(agent);
    }

    @Override
    public void action(){
        DataStore ds = getDataStore();
        ((Logger) getDataStore().get("logger")).info("UPPING THE PRICE");
        double price = (double) ds.get("acceptable_price");
        ds.put("acceptable_price", price + 100);
    }
};
