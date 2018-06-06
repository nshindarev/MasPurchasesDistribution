package purchases.distribution.appl;

import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.Util.CityParserGml;


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
