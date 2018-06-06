package purchases.distribution.appl.Agents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
    public DriverBehaviour(Agent agent){
        super(agent, ParallelBehaviour.WHEN_ANY);
        GenerateProposal genprop = new GenerateProposal(agent, "request-drop");
        CollectResponses collect = new CollectResponses(agent, "request-drop");

        genprop.setDataStore(getDataStore());
        collect.setDataStore(getDataStore());

        addSubBehaviour(genprop);
        addSubBehaviour(collect);
        addSubBehaviour(new PrintRoute(agent, 5000));
    }
};

public class DriverAgent extends Agent {

    public static final Logger logger = LoggerFactory.getLogger(DriverAgent.class);

    public double calculateDeviationCost(String point){
        List<VertexStatus> newWay = updNewWay(point);
        return countWayPrice(newWay) - countCurWayPrice();
    }

    public void addImportantPoint(String point){
        curWay = updNewWay(point);
    }

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

        addBehaviour(new DriverBehaviour(this));

        /**
         *
         *  НИЖЕ ИНИЦИАЛИЗАЦИЯ ИЗ CITIZEN AGENT
         *
         */

        logger.info("Agent " + getAID().getName() + " created");

        Object[] args = getArguments();
        if (args.length >= 1) {
            for(Object obj: args){
                ownWay.add(new VertexStatus((String)obj, Status.MAIN));
            }
            if (args.length == 1) {
                isDriver = false;
            }

            curWay = new LinkedList<>(ownWay);
            checkCyclicWays();
        }
        else {
            logger.error("args for agents " + getAID().getName() + " set incorrect");
            logger.error("agent " + getAID().getName() + " will be destroyed");
            this.doDelete();
        }
    }

    public DriverAgent (List<String> mainVertices){

        this.ownWay = new LinkedList<>();
        this.curWay = new LinkedList<>();

        for(String v: mainVertices){
            ownWay.add(new VertexStatus(v, Status.MAIN));
            curWay.add(new VertexStatus(v, Status.MAIN));
        }
    }
    public DriverAgent (){
        this.ownWay = new LinkedList<>();
        this.curWay = new LinkedList<>();
    }

    public void setCurWay(List<String> way){
        if (curWay.size() == 0){
            this.curWay = new LinkedList<>();
            for(String str: way){
                VertexStatus st = new VertexStatus(str, Status.CURRENT);
                this.curWay.add(st);
            }
        }
        checkCyclicWays();
    }

    /**
     *  koef       = затрата на путь / текущая валюта;
     *  curPayment = сколько зарабатывает на доставке в данный момент;
     *  goods      = сколько товара имеет на руках;
     */
    private double curPayment = 0;
    private double koef = DataPool.koef;
    private int goods;

    /**
     *  содержат только необходимые для посещения вершины в определенном порядке
     *  ownWay  = персональные точки, которые обязательно посещает агент
     *  curWay  = точки с учетом доставки
     */
    private List<VertexStatus> ownWay;
    private List<VertexStatus> curWay;


    public List<VertexStatus> getOwnWay(){
        return this.ownWay;
    }
    public List<VertexStatus> getCurWay(){
        return this.curWay;
    }

    /**
     *  isDriver == true, когда агент можнт перемещаться по графу
     */
    private boolean isDriver = true;

    /**
     * проверяет, содержит ли текущий путь вершину name
     * @param name
     * @return
     */
    private boolean curWayContains (String name){
        for(VertexStatus status: curWay){
            if (status.name.equals(name)) return true;
        }
        return false;
    }

    /**
     * маршруты агентов должны быть цикличны:
     * метод предназначен для проверки на совпадение начальной и конечной точек пути
     * в случае несовпадения маршрут обновляется
     */
    public void checkCyclicWays(){
        if (this.curWay.size() > 0){

            // текущий путь с новыми вершинами
            if (!this.curWay.get(0).name.
                    equals(this.curWay.get(this.curWay.size()-1).name)){
                this.curWay.add(this.curWay.get(0));
            }
        }
        if (this.ownWay.size() > 0){

            // собственный маршрут агента
            if (!this.ownWay.get(0).name.
                    equals(this.ownWay.get(this.ownWay.size()-1).name)){
                this.ownWay.add(this.ownWay.get(0));
            }
        }
    }

    /**
     * Метод высчитывает новый маршрут для агента с учетом добавляемой вершины.
     * в случае, когда вершины не существует || агент инвалид || вершина уже есть
     * то мы ее не добавляем и возвращаем старый маршрут.
     * @param newPoint точка маршрута, которую планируем добавить
     * @return оптимальная последовательность точек маршрута
     */
    public List<VertexStatus> updNewWay (String newPoint){
        checkCyclicWays();

        VertexStatus newPointStatus = new VertexStatus(newPoint, Status.GET);
        this.curWay.add(newPointStatus);

        List<VertexStatus> newWay = new LinkedList<>();

        for(VertexStatus vs: this.ownWay){
            newWay.add(vs);
        }

        // выбираем ближайшую вершину к фиксированному маршруту
        double min = Double.MAX_VALUE;
        VertexStatus startOfShortestPath = newWay.get(0);

        for (VertexStatus vs: this.curWay){
            if(!newWay.contains(vs) && !newWay.get(0).equals(vs)){
                for(VertexStatus fixedVertice: newWay){

                    double distance = DataPool.getShortestPaths().shortestDistance(fixedVertice.name, vs.name);
                    if (distance < min){
                        startOfShortestPath = fixedVertice;
                        min = distance;
                    }
                }


                newWay.add(newWay.indexOf(startOfShortestPath)+1, vs);
                logger.debug(vs.name + " added to path "+ newWay.toString());

                //this.curWay = new LinkedList<>(newWay);
            }
            else logger.trace("way "+ this.curWay.toString()+ " already contains "+ vs.name);
        }

        return newWay;
    }

    @Deprecated
    public List<VertexStatus> getNewWay (String newPoint){

        if (!curWayContains(newPoint) && DataPool.getMyCity().vertexSet().contains(newPoint) && isDriver){
            int minLength = Integer.MAX_VALUE;
            List<VertexStatus> minWay = new LinkedList<>();

            for(int i = 1; i< this.curWay.size()+1; i++){
                int length = 0;

                List<VertexStatus> updWay = new LinkedList<>(this.curWay);
                updWay.add(i, new VertexStatus(newPoint, Status.CURRENT));

                logger.trace(updWay.toString());

                for (int nodeId=0 ; nodeId<updWay.size()-1; nodeId++){
                    length += DataPool.getShortestPaths().shortestDistance(updWay.get(nodeId).name, updWay.get(nodeId+1).name);
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

    /**
     * метод считает затраты для текущего пути curWay
     * @return цена "на бензин"
     */
    public double countCurWayPrice (){
        double price = 0;
        for (int i = 0; i < curWay.size()-1; i++){

            Iterator<MyWeightedEdge> edges =  DataPool.getShortestPaths().
                    getShortestPath(curWay.get(i).name, curWay.get(i+1).name).getEdgeList().iterator();
            while(edges.hasNext()){
                price += edges.next().get_weight();
            }
        }
        return price;
    }

    /**
     * считает затраты на основе пути в графе
     * @param nodesInPath вершины, через которые нужно проехать
     * @return цена "на бензин"
     */
    public double countWayPrice (List<VertexStatus> nodesInPath){

        double price = 0;
        for (int i = 0; i<nodesInPath.size()-1; i++){
            Iterator<MyWeightedEdge> edges =  DataPool.getShortestPaths().
                    getShortestPath(nodesInPath.get(i).name, nodesInPath.get(i+1).name).getEdgeList().iterator();

            while(edges.hasNext()){
                price += edges.next().get_weight();
            }
        }
        return price;
    }

    /**
     * высчитывает: принимаем мы предложение или нет
     * @param offer предложение
     * @return true, если предложение выгодное
     */
    public boolean beneficialOffer (Offer offer){
        List<VertexStatus> optimalWay = getNewWay(offer.newNode);

        double opt = this.countWayPrice(optimalWay);
        double cur = this.countWayPrice(curWay);

        if (this.countWayPrice(optimalWay) - offer.payment*koef < this.countCurWayPrice()){
            return true;
        }
        return false;
    }

    /**
     * фиксация транзакции о доставке товара
     * @param offer предложение по доставке
     */
    public void approveOffer (Offer offer){
        if (beneficialOffer(offer)){
            this.curWay = getNewWay(offer.newNode);
            this.curPayment += offer.payment;
            this.goods += 1;
        }
    }

    /**
     * подсчитывает суммарные затраты на дорогу
     * @return затраты
     */
    public double evaluateRezCost(){
        if (!isDriver){
            return 0;
        }
        return  curPayment*koef - countCurWayPrice();
    }

    public void printWay(){
        logger.info(getCurWay().toString());
    }
}
