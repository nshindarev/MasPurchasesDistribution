package purchases.distribution.appl.Util;


import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

import java.io.File;
import java.util.List;

public class DataPool{
    private DataPool(){
    }
    public static final double koef = 1;

    private static File cityFile;
    private static City<String, MyWeightedEdge> myCity;
    private static FloydWarshallShortestPaths<String, MyWeightedEdge> allPaths;
    private static String storageName;

    public static void setMyCity( City<String, MyWeightedEdge> myCity) {
        if (DataPool.myCity == null)
            DataPool.myCity = myCity;
    }
    public static City<String, MyWeightedEdge> getMyCity(){
        return DataPool.myCity;
    }
    public static void setMyCityFile(File file){
        if (DataPool.cityFile == null){
            DataPool.cityFile = file;
        }
    }
    public static File getCityFile (){
        return DataPool.cityFile;
    }
    public  static FloydWarshallShortestPaths<String, MyWeightedEdge> getShortestPaths(){
        if (allPaths == null){
            DataPool.allPaths = new FloydWarshallShortestPaths(getMyCity());
        }
        return allPaths;
    }

    public static void setStorageName(String name){
        storageName = name;
    }
    public static String getStorageName(){
        return storageName;
    }
}
