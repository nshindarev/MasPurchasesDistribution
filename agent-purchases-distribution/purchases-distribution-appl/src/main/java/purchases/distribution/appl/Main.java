package purchases.distribution.appl;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        jade.Boot.main(new String[] {
                "-gui",
                "god:dreamteam.carpooling.appl.Util.CreatorAgent()"
        });


        logger.info("application started");

        /**
         *  парсим граф из файла
         */

        CityParserGml parser =  new CityParserGml();
        parser.parseCityFromFile();

        FloydWarshallShortestPaths<String,  DefaultWeightedEdge> flWar =
                (FloydWarshallShortestPaths<String,  DefaultWeightedEdge>)DataPool.getInstance().getShortestPaths();

        logger.info(flWar.getShortestPath("1","10").toString());
    }
}
