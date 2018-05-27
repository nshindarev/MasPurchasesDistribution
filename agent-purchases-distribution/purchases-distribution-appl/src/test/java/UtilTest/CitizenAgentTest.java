package UtilTest;

import org.jgrapht.ext.ImportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.CitizenAgent;
import purchases.distribution.appl.Util.CityParserGml;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CitizenAgentTest {
    private static final Logger logger = LoggerFactory.getLogger(CitizenAgentTest.class);

    @Test
    public void addNewVertexTest() throws IOException {
        String s = new File( "." ).getCanonicalPath();
        logger.info(s);

        CitizenAgent testAgent = new CitizenAgent();

        testAgent.setCurWay(Arrays.asList("1", "5", "8"));
        logger.debug("test new way for testAgent");

        ArrayList<String> list = new ArrayList<String>() {{
            add("0");
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
            add("9");
        }};

        for (String newItemTesting: list){
            try{
                CityParserGml parser =  new CityParserGml(new File("src/main/resources/small_city.gml"));
                parser.parseCityFromFile();

                List<String> rez = testAgent.getNewWay(newItemTesting);

                logger.trace("test rezult for item: " + newItemTesting);
                logger.trace(rez.toString());

            }
            catch (ImportException ex){
                logger.error("test adding new vertex crashed: cannot load city");
            }
        }
    }

}
