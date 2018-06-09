package purchases.distribution.appl;

import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.ArgsParser;
import purchases.distribution.appl.Util.CityParserGml;
import purchases.distribution.appl.Util.DataPool;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;


public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args){
        try{
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


            logger.info("application started");
            jade.Boot.main(new String[] {
                    "-gui",
                    "god:purchases.distribution.appl.Util.CreatorAgent"
            });
        }
        catch (org.apache.commons.cli.ParseException ex){
            logger.error(ex.getMessage());
        }
    }
}
