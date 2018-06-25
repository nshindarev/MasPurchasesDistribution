package purchases.distribution.appl;

import org.apache.commons.cli.ParseException;
import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.ArgsParser;
import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;

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

        logger.info("application started");
        jade.Boot.main(new String[] {
                "-gui",
                "god:purchases.distribution.appl.Util.CreatorAgent"
        });
    }
}
