package UtilTest;

import org.jgrapht.ext.ImportException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import purchases.distribution.appl.RouteHandler;
import purchases.distribution.appl.Util.*;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class RouteHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(RouteHandlerTest.class);

    private RouteHandler testAgent;
    private Set<String> vertexSet;

    @Before
    public void setCityForTest()throws IOException {
        String s = new File( "." ).getCanonicalPath();
        logger.info(s);

        try{
        CityParserGml parser =  new CityParserGml(new File("src/main/resources/small_city.gml"));
            parser.parseCityFromFile();
            logger.info("city initialized successfully");

            this.vertexSet = DataPool.getMyCity().vertexSet();
            logger.trace("city contains " + vertexSet.size() + " vertices");
        }
        catch (ImportException ex){
            logger.error(ex.getMessage());
        }


    }

    @Test
    public void checkWayUpdate(){
        this.testAgent = new RouteHandler(Arrays.asList("9", "3", "6", "8", "1"));
        logger.debug(testAgent.getOwnWay().toString());

        for(String point: this.vertexSet){
            logger.info(this.testAgent.updNewWay(new VertexStatus(point, Status.DELIVER)).toString());
            logger.debug(testAgent.getCurWay().toString());
        }
    }

    @Ignore
    @Test
    public void countCurPriceTest(){
        this.testAgent = new RouteHandler();
        testAgent.setCurWay(Arrays.asList("1", "3", "6", "8"));

        logger.debug("test results for countCurPriceTest: ");
        logger.debug(Double.toString(this.testAgent.countCurWayPrice()));

        logger.trace(Double.toString(this.testAgent.countWayPrice(Arrays.asList(new VertexStatus("1", Status.MAIN),
                new VertexStatus("3", Status.MAIN),
                new VertexStatus("6", Status.MAIN),
                new VertexStatus("8", Status.MAIN)))));
    }

    @Ignore
    @Test
    public void testBenefitCounter (){
        this.testAgent = new RouteHandler();
        testAgent.setCurWay(Arrays.asList("1", "3", "6", "8"));

        logger.debug("test results for testBenefitCounter: ");
        logger.debug(Boolean.toString(this.testAgent.beneficialOffer(new Offer("11", 1000))));
        logger.debug(Boolean.toString(this.testAgent.beneficialOffer(new Offer("11", 10))));

    }

    @Test
    public void checkCyclicPathTest(){
        this.testAgent = new RouteHandler();
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
