package purchases.distribution.appl;

import jade.core.Agent;

import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;
import purchases.distribution.appl.Util.DataPool;
import purchases.distribution.appl.Util.Offer;
import purchases.distribution.appl.Util.Status;
import purchases.distribution.appl.Util.VertexStatus;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RouteHandler extends Agent {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(RouteHandler.class);


    public RouteHandler(List<String> mainVertices){

        this.ownWay = new LinkedList<>();
        this.curWay = new LinkedList<>();

        for(String v: mainVertices){
            ownWay.add(new VertexStatus(v, Status.MAIN));
            curWay.add(new VertexStatus(v, Status.MAIN));
        }
    }
    public RouteHandler(){
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
    public List<VertexStatus> ownWay;
    public List<VertexStatus> curWay;

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

    protected void setup(){
        super.setup();
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

                this.curWay = new LinkedList<>(newWay);
            }
            else logger.trace("way "+ this.curWay.toString()+ " already contains "+ vs.name);
        }

        return newWay;
    }
    public List<VertexStatus> updNewWay (VertexStatus newPoint){

        checkCyclicWays();

        if(newPoint.isGet()||newPoint.isDeliver()){
            this.curWay.add(newPoint);

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

                    this.curWay = new LinkedList<>(newWay);
                }
                else logger.trace("way "+ this.curWay.toString()+ " already contains "+ vs.name);
            }

            return newWay;
        }
        else return new LinkedList<>();
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
        List<VertexStatus> optimalWay = updNewWay(offer.newNode);

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
            this.curWay = updNewWay(offer.newNode);
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

    @Override
    public String toString(){
        return curWay.toString();
    }
}