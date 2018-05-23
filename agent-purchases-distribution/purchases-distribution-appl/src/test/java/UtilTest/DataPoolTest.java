package UtilTest;

import org.jgrapht.ext.ImportException;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import purchases.distribution.appl.Util.CityParserGml;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(DataPoolTest.class);

    @Test
    public void checkDefaultFileLocation() throws ImportException{
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        logger.info("Current relative path is: " + s);

        CityParserGml parser =  new CityParserGml();
        parser.parseCityFromFile();

        parser.getCity();
    }

    @Test
    public void checkFileLocation() throws ImportException{
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        logger.info("Current relative path is: " + s);

        CityParserGml parser =  new CityParserGml(new File("src/main/resources/small_city_equal_weight.gml"));
        parser.parseCityFromFile();

        parser.getCity();
    }
}
