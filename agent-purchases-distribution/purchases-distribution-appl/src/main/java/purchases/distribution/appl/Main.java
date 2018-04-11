package purchases.distribution.appl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CityParserGml;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        logger.info("application started");
        new CityParserGml();
    }
}
