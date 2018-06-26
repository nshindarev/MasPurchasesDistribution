package purchases.distribution.appl.Util;

import java.io.*;
import jade.core.Agent;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import purchases.distribution.appl.Behaviours.*;

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
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/grid40.txt"))) {
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
        final int driver_num = drivers;
        addBehaviour(new Collector(this, drivers, "deviation"){
            private double total = 0;

            @Override
            public void handle(ACLMessage msg){
                total += Double.parseDouble(msg.getContent());
            }

            @Override
            public int onEnd(){
                logger.info("Total deviation: " + total);
                return 0;
            }
        });
        addBehaviour(new Collector(this, pedestrians, "im-done"){
            @Override
            public int onEnd(){
                myAgent.addBehaviour(new Broadcast(myAgent, ACLMessage.INFORM, "phase-two"){
                    @Override
                    public String getContent(){ return String.valueOf(driver_num); }
                });
                return 0;
            }
        });
        addBehaviour(new Collector(this, drivers, "pedestrian-deviation"){
            private double total = 0;

            @Override
            public void handle(ACLMessage msg){
                total += Double.parseDouble(msg.getContent());
            }

            @Override
            public int onEnd(){
                logger.info("Total 'pedestrian' deviation: " + total);
                return 0;
            }
        });

    }
}
