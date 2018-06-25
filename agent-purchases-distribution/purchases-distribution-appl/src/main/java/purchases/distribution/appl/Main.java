package purchases.distribution.appl;

import org.apache.commons.cli.ParseException;
import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.ArgsParser;
import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;
import purchases.distribution.appl.Util.Route;
import java.util.*;

import java.io.File;


public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws ParseException{

        // считываем аргументы запуска
        ArgsParser argsParser = new ArgsParser(args);
        argsParser.parseOptions();


        CityParserGml parser =  new CityParserGml(DataPool.getCityFile(),DataPool.getAgentsFile());

        try{
            parser.parseCityFromFile();
            logger.info("city initialized successfully");
        }
        catch (ImportException ex){
            logger.error(ex.getMessage());
        }

        // ткнуть, чтобы сразу построились все кратчайшие пути
        for(String n1 : DataPool.getMyCity().vertexSet())
        for(String n2 : DataPool.getMyCity().vertexSet())
            DataPool.getShortestPaths().shortestDistance(n1, n2);

        //Route route1 = new Route(Arrays.asList("32", "64", "29"), "56", new HashSet<String>());
        //Route route1 = new Route(Arrays.asList("11", "15", "25"), "56", new HashSet<String>());
        //logger.info(route1.toString());
        //logger.info(route1.expand().toString());

        //Route route2 = new Route(Arrays.asList("37", "67", "34", "18"), "56", new HashSet<String>());
        //logger.info(route2.toString());
        //logger.info(route2.expand().toString());

        //Route route3 = new Route(Arrays.asList("79", "85", "52"), "56", new HashSet<String>());
        //logger.info(route3.toString());
        //logger.info(route3.expand().toString());

        //logger.info(route1.optimalMiddlePoint(route2));
        //logger.info(route1.optimalMiddlePoint(route3));
        //logger.info(route2.optimalMiddlePoint(route3));


        logger.info("application started");
        jade.Boot.main(new String[] {
                "-gui",
                "god:purchases.distribution.appl.Util.CreatorAgent"
        });
    }
}
