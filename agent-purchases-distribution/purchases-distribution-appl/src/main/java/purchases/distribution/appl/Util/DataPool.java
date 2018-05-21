package purchases.distribution.appl.Util;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;

import java.util.LinkedList;
import java.util.List;

public class DataPool<V,E> {
    /**
     *  singleton класс:
     *      - город сохраняется при создании объекта класса DataPool
     *      - есть только один экземпляр DataPool в проекте
     */

    private static DataPool<String,MyWeightedEdge> instance;
    private DataPool (){

    }

    public static synchronized DataPool<String,MyWeightedEdge> getInstance(){
        if (instance == null){
            instance = new DataPool<String,MyWeightedEdge>();
            return instance ;
        }
        else return instance;
    }

    /**
     *   myCity - final field
     */

    private City<V,E> myCity;

    public void setMyCity(City<V, E> myCity) {
        if (this.myCity == null)
            this.myCity = myCity;
    }
    public City<V,E> getMyCity(){
        return this.myCity;
    }

    /**
     *
     * Shortest paths: Floyd Warshall
     */
    private FloydWarshallShortestPaths<V, E> allPaths;
    public FloydWarshallShortestPaths<V, E> getShortestPaths(){
        if (allPaths == null){
            this.allPaths = new FloydWarshallShortestPaths(getMyCity());
        }
        return allPaths;
    }
    public List<V> getShortestPath(V a, V b){
       return getShortestPaths().getShortestPath(a, b).getVertexList();
    }
}
