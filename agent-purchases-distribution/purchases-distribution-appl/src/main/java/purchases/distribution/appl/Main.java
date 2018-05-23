package purchases.distribution.appl;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {

        /**
         *  считываем граф из файла в datapool
         */

        CityParserGml parser =  new CityParserGml();
        try{
            parser.parseCityFromFile();
            logger.info("city initialized successfully");
        }
        catch (ImportException ex){
            logger.error(ex.getMessage());
        }

        logger.info("application started");
        jade.Boot.main(new String[] {
                "-gui",
                "god:purchases.distribution.appl.Util.CreatorAgent"
        });
    }
}
