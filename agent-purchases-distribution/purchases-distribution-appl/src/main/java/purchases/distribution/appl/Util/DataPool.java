package purchases.distribution.appl.Util;


import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DataPool{
    // --------------------------------logger---------------------------------------
    public static final Logger logger = LoggerFactory.getLogger(CityParserGml.class);

    //---constructor---
    private DataPool(){
    }

    //---------fields------------------
    public static final double koef = 1;
    private static File cityFile;
    private static City<String, MyWeightedEdge> myCity;
    private static FloydWarshallShortestPaths<String, MyWeightedEdge> allPaths;
    private static String storageName;
    private static List<List<String>> agentsPaths;

    // объект - город
    public static void setMyCity( City<String, MyWeightedEdge> myCity) {
        if (DataPool.myCity == null)
            DataPool.myCity = myCity;
    }
    public static City<String, MyWeightedEdge> getMyCity(){
        return DataPool.myCity;
    }

    // файл с городом
    public static void setMyCityFile(File file){
        if (DataPool.cityFile == null){
            DataPool.cityFile = file;
        }
    }
    public static File getCityFile (){
        return DataPool.cityFile;
    }

    // хранилище
    public static void setStorageName(String name){
        storageName = name;
    }
    public static String getStorageName(){
        return storageName;
    }

    // пути агентов
    public static void setAgentsPaths (List<List<String>> agentsPaths){
        if (DataPool.agentsPaths == null){
            DataPool.agentsPaths = new LinkedList<>(agentsPaths);
        }
    }
    public static List<List<String>> getAgentsPaths(){
        return agentsPaths;
    }



    public  static FloydWarshallShortestPaths<String, MyWeightedEdge> getShortestPaths(){
        if (allPaths == null){
            DataPool.allPaths = new FloydWarshallShortestPaths(getMyCity());
        }
        return allPaths;
    }


}
