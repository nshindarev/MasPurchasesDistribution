package purchases.distribution.appl.Util;

import java.io.*;
import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;

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
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/grid10.txt"))) {
            String line;
            DataPool.setStorageName(br.readLine());
            while ((line = br.readLine()) != null) {
                logger.debug("looking at " + line);
                String[] args = line.split(" ");
                if(args.length == 1)
                    createAgent("Pedestrian" + (++pedestrians), "purchases.distribution.appl.Agents.PedestrianAgent", args);
                else
                    createAgent("Driver" + (++drivers), "purchases.distribution.appl.Agents.DriverAgent", args);
            }
        } catch(IOException ex){
            logger.error("some error");
        }
    }
}
