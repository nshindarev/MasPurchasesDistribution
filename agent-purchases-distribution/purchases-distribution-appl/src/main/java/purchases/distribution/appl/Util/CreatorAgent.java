package purchases.distribution.appl.Util;

import java.io.*;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class CreatorAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(CreatorAgent.class);

    private void createAgent(String name, String type, Object[] args){
        try {
            ContainerController cc = getContainerController();
            AgentController agent  = cc.createNewAgent(name, type, args);

            agent.start();
        }
        catch (StaleProxyException ex){
            logger.error(ex.getLocalizedMessage());
        }
    }

    @Override
    public void setup() {
        int pedestrians = 0, drivers = 0;
        logger.info("gon do sum");
        logger.debug("currently at: " + Paths.get(".").toAbsolutePath().normalize().toString());



        if(DataPool.getAgentsPaths().size()>0){
            DataPool.setStorageName(DataPool.getAgentsPaths().get(0).toString());
            DataPool.getAgentsPaths().remove(0);

            for(List<String> agentPoints: DataPool.getAgentsPaths()){
                logger.debug("looking at " + agentPoints.toString());
                if(agentPoints.size() == 1)
                        createAgent("Pedestrian" + (++pedestrians), "purchases.distribution.appl.Agents.PedestrianAgent", agentPoints.toArray());
                    else
                        createAgent("Driver" + (++drivers), "purchases.distribution.appl.Agents.DriverAgent", agentPoints.toArray());
                }
            }
        }

}
