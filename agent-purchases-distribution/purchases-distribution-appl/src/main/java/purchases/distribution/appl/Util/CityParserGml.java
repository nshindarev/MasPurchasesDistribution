package purchases.distribution.appl.Util;


import org.jgrapht.ext.GmlImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class CityParserGml {
    public static final Logger logger = LoggerFactory.getLogger(City.class);

    private File cityGmlFile;
    private City<String, MyWeightedEdge> city = new City(DefaultWeightedEdge.class);



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
            this.cityGmlFile = new File("purchases-distribution-appl/src/main/resources/small_city.gml");
            logger.info("city downloaded from file: {}", this.cityGmlFile.getName());

        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }

    public void parseCityFromFile(){
        this.city = new City<String, MyWeightedEdge>(MyWeightedEdge.class);


        GmlImporter<String,MyWeightedEdge> importer =
                new GmlImporter<>(
                        (String label, Map<String, String> attributes)
                                -> {return label;},
                        (String from, String to, String label, Map<String, String> attributes)
                                -> { return city.getEdgeFactory().createEdge(from, to); });



        try{
            importer.importGraph(this.city, this.cityGmlFile);
            updateDataPool(this.city);
        }
        catch (ImportException ex){
            logger.error("сломалось при чтении из файла " + cityGmlFile.getPath());
        }
    }
    public void updateDataPool(City<String, MyWeightedEdge> city){
        DataPool.getInstance().setMyCity(city);

    }
}
