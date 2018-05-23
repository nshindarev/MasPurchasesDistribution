package purchases.distribution.appl.Util;


import org.jgrapht.ext.GmlImporter;
import org.jgrapht.ext.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

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
        try {
            this.cityGmlFile = new File("src/main/resources/small_city.gml");
            logger.info("city downloaded from file: {}", this.cityGmlFile.getName());
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }

    public City getCity(){
        return this.city;
    }
    public void parseCityFromFile() throws ImportException{

        GmlImporter<String,MyWeightedEdge> importer =
                new GmlImporter<>(
                        (String label, Map<String, String> attributes)
                                -> {return label;},
                        (String from, String to, String label, Map<String, String> attributes)
                                -> { return city.getEdgeFactory().createEdge(from, to); });

        importer.importGraph(this.city, this.cityGmlFile);
        updateDataPool(this.city);
    }

    public void updateDataPool(City<String, MyWeightedEdge> city){
        DataPool.getInstance().setMyCity(city);
    }
}
