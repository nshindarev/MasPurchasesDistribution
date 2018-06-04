package UtilTest;

import org.jgrapht.ext.ImportException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.CitizenAgent;
import purchases.distribution.appl.Util.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CitizenAgentTest {
    private static final Logger logger = LoggerFactory.getLogger(CitizenAgentTest.class);
    private CitizenAgent testAgent;

    @Before
    public void setCityForTest()throws IOException {
        String s = new File( "." ).getCanonicalPath();
        logger.info(s);

        try{
        CityParserGml parser =  new CityParserGml(new File("src/main/resources/small_city.gml"));
            parser.parseCityFromFile();
            logger.info("city initialized successfully");
        }
        catch (ImportException ex){
            logger.error(ex.getMessage());
        }

        //this.testAgent = new CitizenAgent();
        //testAgent.setCurWay(Arrays.asList("1", "5", "8"));

    }

    @Test
    public void checkWayUpdate(){
        this.testAgent = new CitizenAgent(Arrays.asList("1", "3", "6", "8"));
        logger.debug(testAgent.getOwnWay().toString());

        this.testAgent.getNewWay("4");
        this.testAgent.updNewWay("5");

        logger.debug(testAgent.getCurWay().toString());
    }

    @Ignore
    @Test
    public void countCurPriceTest(){
        this.testAgent = new CitizenAgent();
        testAgent.setCurWay(Arrays.asList("1", "3", "6", "8"));

        logger.debug("test results for countCurPriceTest: ");
        logger.debug(Double.toString(this.testAgent.countCurWayPrice()));

        logger.trace(Double.toString(this.testAgent.countWayPrice(Arrays.asList(new VertexStatus("1", Status.MAIN),
                new VertexStatus("3", Status.MAIN),
                new VertexStatus("6", Status.MAIN),
                new VertexStatus("8", Status.MAIN)))));
    }

    @Test
    public void addNewVertexTest() {

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
            List<VertexStatus> rez = testAgent.getNewWay(newItemTesting);

            logger.debug("test rezult for item: " + newItemTesting);
            logger.debug(rez.toString());
        }
    }

    @Ignore
    @Test
    public void testBenefitCounter (){
        this.testAgent = new CitizenAgent();
        testAgent.setCurWay(Arrays.asList("1", "3", "6", "8"));

        logger.debug("test results for testBenefitCounter: ");
        logger.debug(Boolean.toString(this.testAgent.beneficialOffer(new Offer("11", 1000))));
        logger.debug(Boolean.toString(this.testAgent.beneficialOffer(new Offer("11", 10))));

    }

    @Test
    public void checkCyclicPathTest(){
        this.testAgent = new CitizenAgent();
        testAgent.setCurWay(Arrays.asList("1", "3", "6", "8"));

        testAgent.checkCyclicWays();
        logger.debug(testAgent.getOwnWay().toString());
        logger.debug(testAgent.getCurWay().toString());
    }

    @Ignore
    @Test
    public void visualizeTest(){
        GraphVisualize gv = new GraphVisualize();
        gv.init();
    }
}
