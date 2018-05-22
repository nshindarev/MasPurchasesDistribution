package purchases.distribution.appl.Util;


import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

public class DataPool {
    /**
     *  singleton класс:
     *      - город сохраняется при создании объекта класса DataPool
     *      - есть только один экземпляр DataPool в проекте
     */

    private static DataPool instance;
    private DataPool (Class vertex_type, Class edge_type){
        this.v = vertex_type;
        this.e = edge_type;
    }

    public static synchronized DataPool getInstance(Class vertex_type, Class edge_type){

        if (instance == null){
            instance = new DataPool(vertex_type, edge_type);
        }

        return instance;
    }

    /**
     * data fields
     *  - v vertex type
     *  - e edge type
     *
     */

    private Class<v> v;
    private Class<e> e;

    private City<? extends v, ? extends e> myCity;

    public void setMyCity(City<? extends v, ? extends e> myCity) {
        if (this.myCity == null)
            this.myCity = myCity;
    }
    public City<? extends v, ? extends e> getMyCity(){
        return this.myCity;
    }

    /**
     * Shortest paths: Floyd Warshall
     */

    private FloydWarshallShortestPaths<? extends v, ? extends e> allPaths;
    public  FloydWarshallShortestPaths<? extends v, ? extends e> getShortestPaths(){
        if (allPaths == null){
            this.allPaths = new FloydWarshallShortestPaths(getMyCity());
        }
        return allPaths;
    }

    /*
    public List<? extends v> getShortestPath(Object a, Object b){
       if (v.isInstance(a) && v.isInstance(b)){
           v.cast(a);
       }
       return getShortestPaths().getShortestPath(v.cast(a), v.cast(b)).getVertexList();
    }*/
}
