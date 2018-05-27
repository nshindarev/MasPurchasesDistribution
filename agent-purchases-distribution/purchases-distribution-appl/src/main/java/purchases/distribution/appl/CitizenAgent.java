package purchases.distribution.appl;

import jade.core.Agent;

import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;
import purchases.distribution.appl.Util.DataPool;
import purchases.distribution.appl.Util.Offer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CitizenAgent extends Agent {

    public static final org.slf4j.Logger logger = LoggerFactory.getLogger(CitizenAgent.class);
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
    private List<String> ownWay;
    private List<String> curWay;

    public List<String> getOwnWay(){
        return this.ownWay;
    }
    public List<String> getCurWay(){
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
                ownWay.add((String)obj);
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
     * маршруты агентов должны быть цикличны:
     * метод предназначен для проверки на совпадение начальной и конечной точек пути
     */
    public void checkCyclicWays(){
        if (this.curWay.size() > 0){

            // текущий путь с новыми вершинами
            if (!this.curWay.get(0).
                    equals(this.curWay.get(this.curWay.size()-1))){
                this.curWay.add(this.curWay.get(0));
            }
        }
        if (this.ownWay.size() > 0){

            // собственный маршрут агента
            if (this.ownWay.get(0).
                    equals(this.ownWay.get(this.ownWay.size()-1))){
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
    public List<String> getNewWay (String newPoint){

        if (!curWay.contains(newPoint) && DataPool.getMyCity().vertexSet().contains(newPoint) && isDriver){
            int minLength = Integer.MAX_VALUE;
            List<String> minWay = new LinkedList<>();

            for(int i = 1; i< this.curWay.size()+1; i++){
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

    /**
     * метод считает затраты для текущего пути curWay
     * @return цена "на бензин"
     */
    public double countCurWayPrice (){
        if (!isDriver){
            return 0;
        }
        else{
            double price = 0;
            for (int i = 0; i<curWay.size()-1; i++){

                Iterator<MyWeightedEdge> edges =  DataPool.getShortestPaths().
                            getShortestPath(curWay.get(i), curWay.get(i+1)).getEdgeList().iterator();
                while(edges.hasNext()){
                    price += edges.next().get_weight();
                }
            }
            return price;
        }
    }

    /**
     * считает затраты на основе пути в графе
     * @param nodesInPath вершины, через которые нужно проехать
     * @return цена "на бензин"
     */
    public double countWayPrice (List<String> nodesInPath){

        double price = 0;
        for (int i = 0; i<nodesInPath.size()-1; i++){
            Iterator<MyWeightedEdge> edges =  DataPool.getShortestPaths().
                        getShortestPath(nodesInPath.get(i), nodesInPath.get(i+1)).getEdgeList().iterator();

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
        List<String> optimalWay = getNewWay(offer.newNode);

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
}