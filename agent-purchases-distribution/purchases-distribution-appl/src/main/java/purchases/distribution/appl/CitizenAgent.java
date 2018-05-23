package purchases.distribution.appl;

import jade.core.Agent;

import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CreatorAgent;

import java.util.logging.Logger;

public class CitizenAgent extends Agent {
    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(CreatorAgent.class);

    protected void setup(){
        logger.info("Agent " + getAID().getName() + " created");
    }
}
