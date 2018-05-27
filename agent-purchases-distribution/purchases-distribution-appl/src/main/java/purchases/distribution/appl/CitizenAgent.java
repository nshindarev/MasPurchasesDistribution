package purchases.distribution.appl;

import jade.core.Agent;

import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CreatorAgent;
import purchases.distribution.appl.Util.DataPool;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class CitizenAgent extends Agent {

    public CitizenAgent (){
        this.ownWay = new LinkedList<>();
        this.curWay = new LinkedList<>();
    }
    public void setCurWay(List<String> way){
        if (curWay.size() == 0){
            this.curWay = new LinkedList<>();
            for(String str: way){
                this.curWay.add(str);
            }
        }
    }
    /**
     *  koef = затрата на путь / текущая валюта;
     */

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(CreatorAgent.class);
    private double koef = DataPool.koef;

    /**
     *  содержат только необходимые для посещения вершины в определенном порядке
     */
    private List<String> ownWay;
    private List<String> curWay;

    /**
     *  isDriver == true, когда агент можнт перемещаться по графу
     */
    private boolean isDriver = true;

    protected void setup(){
        super.setup();
        logger.info("Agent " + getAID().getName() + " created");

        Object[] args = getArguments();
        if (args.length >= 1) {
            for(Object obj: args){
                ownWay.add((String)obj);
            }
            if (args.length == 1) isDriver = false;
        }
        else {
            logger.error("args for agents " + getAID().getName() + " set incorrect");
            logger.error("agent " + getAID().getName() + " will be destroyed");
            this.doDelete();
        }

        getNewWay("2");
    }
    public List<String> getNewWay (String newPoint){

        if (!curWay.contains(newPoint) && DataPool.getMyCity().vertexSet().contains(newPoint)){
            int minLength = Integer.MAX_VALUE;
            List<String> minWay = new LinkedList<>();

            for(int i = 0; i< this.curWay.size()+1; i++){
                int length = 0;

                List<String> updWay = new LinkedList<>(this.curWay);
                updWay.add(i, newPoint);

                logger.trace(updWay.toString());

                for (int nodeId=0 ; nodeId<updWay.size()-1; nodeId++){
                    length += DataPool.getShortestPaths().shortestDistance(updWay.get(nodeId), updWay.get(nodeId+1));
                }
                if (length < minLength)
                {
                    minLength = length;
                    minWay = new LinkedList(updWay);
                }
            }
            return minWay;
        }
        return curWay;
    }

}
