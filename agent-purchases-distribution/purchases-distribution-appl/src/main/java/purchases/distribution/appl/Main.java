package purchases.distribution.appl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.Util.CityParserGml;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        jade.Boot.main(new String[] {
                "-gui",
                "god:dreamteam.carpooling.appl.Util.CreatorAgent()"
        });


        logger.info("application started");
        new CityParserGml();
    }
}
