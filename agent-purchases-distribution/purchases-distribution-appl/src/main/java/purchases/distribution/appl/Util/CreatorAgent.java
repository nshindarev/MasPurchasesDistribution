package purchases.distribution.appl.Util;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.CitizenAgent;

public class CreatorAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(CreatorAgent.class);

    @Override
    public void setup() {

        try{
            ContainerController cc = getContainerController();
            AgentController nick   = cc.createNewAgent("nick",   "purchases.distribution.appl.CitizenAgent", new Object[] {});
        }
        catch (StaleProxyException ex){
            logger.error(ex.getLocalizedMessage());
        }
    }
}
