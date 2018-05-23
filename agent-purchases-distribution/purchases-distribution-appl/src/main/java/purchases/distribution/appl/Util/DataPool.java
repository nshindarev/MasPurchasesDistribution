package purchases.distribution.appl.Util;


import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import purchases.distribution.appl.GraphImplement.City;

import java.util.List;

public class DataPool <V,E>{
    /**
     *  singleton класс:
     *      - город сохраняется при создании объекта класса DataPool
     *      - есть только один экземпляр DataPool в проекте
     */

    private static DataPool instance;
    private DataPool(){
    }

    public static synchronized DataPool getInstance(){

        if (instance == null){
            instance = new DataPool();
        }

        return instance;
    }

    /**
     * data fields
     */

    private City<String, DefaultWeightedEdge> myCity;

    public void setMyCity( City<String, DefaultWeightedEdge> myCity) {
        if (this.myCity == null)
            this.myCity = myCity;
    }

    public City<String, DefaultWeightedEdge> getMyCity(){
        return this.myCity;
    }

    /**
     * Shortest paths: Floyd Warshall
     */

    private FloydWarshallShortestPaths<String, DefaultWeightedEdge> allPaths;
    public  FloydWarshallShortestPaths<String, DefaultWeightedEdge> getShortestPaths(){
        if (allPaths == null){
            this.allPaths = new FloydWarshallShortestPaths(getMyCity());
        }
        return allPaths;
    }


    public List<String> getShortestPath(String a, String b){
       return getShortestPaths().getShortestPath(a, b).getVertexList();
    }
}
