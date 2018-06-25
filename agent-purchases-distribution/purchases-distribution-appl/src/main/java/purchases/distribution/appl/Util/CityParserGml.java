package purchases.distribution.appl.Util;


import org.jgrapht.ext.GmlImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

import java.nio.file.Paths;


import java.io.File;
import java.util.Map;

public class CityParserGml {
    public static final Logger logger = LoggerFactory.getLogger(CityParserGml.class);

    private File cityGmlFile;
    private City<String, MyWeightedEdge> city = new City(MyWeightedEdge.class);



    public CityParserGml(File cityGmlFile){
        try{
            this.cityGmlFile = cityGmlFile;
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }
    public CityParserGml (){
        logger.debug("currently at: " + Paths.get(".").toAbsolutePath().normalize().toString());
        try {
            this.cityGmlFile = new File("src/main/resources/grid40.gml");
            logger.info("city downloaded from file: {}", this.cityGmlFile.getName());

            DataPool.setMyCityFile(this.cityGmlFile);
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }

    public City getCity(){
        return this.city;
    }
    public void parseCityFromFile() throws ImportException{
        if (DataPool.getMyCity() == null || DataPool.getCityFile() == null){
            GmlImporter<String,MyWeightedEdge> importer =
                    new GmlImporter<>(
                            (String label, Map<String, String> attributes)
                                    -> {return label;},
                            (String from, String to, String label, Map<String, String> attributes)
                                    -> { return city.getEdgeFactory().createEdge(from, to); });

            importer.importGraph(this.city, this.cityGmlFile);
            updateDataPool(this.city);
        }
    }

    public void updateDataPool(City<String, MyWeightedEdge> city){
        DataPool.setMyCity(city);
    }
}
