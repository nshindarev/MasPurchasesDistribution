package purchases.distribution.appl;

import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {


        logger.debug(System.getProperty("user.dir").toString());
        CityParserGml parser =  new CityParserGml(new File("agent-purchases-distribution\\purchases-distribution-appl\\src\\main\\resources\\very_small_city.gml"),
                                                  new File("agent-purchases-distribution\\purchases-distribution-appl\\src\\main\\resources\\very_small_city.txt"));
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
