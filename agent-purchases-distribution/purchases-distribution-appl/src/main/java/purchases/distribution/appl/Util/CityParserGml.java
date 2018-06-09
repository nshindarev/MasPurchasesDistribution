package purchases.distribution.appl.Util;


import org.jgrapht.ext.GmlImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;


import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CityParserGml {
    public static final Logger logger = LoggerFactory.getLogger(CityParserGml.class);

    private File cityGmlFile;
    private File agentsConfigFile;
    private City<String, MyWeightedEdge> city = new City(MyWeightedEdge.class);



    public CityParserGml(File cityGmlFile, File agentConfigFile){
        try{
            this.agentsConfigFile = agentConfigFile;
            this.cityGmlFile = cityGmlFile;
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }
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
            this.cityGmlFile = new File("src/main/resources/very_small_city.gml");
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

        if (DataPool.getStorageName() == null){
            LinkedList<List<String>> agentsPaths = new LinkedList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(agentsConfigFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    agentsPaths.add(Arrays.asList(line.split(" ")));
                }
                DataPool.setAgentsPaths(agentsPaths);
            }
            catch (IOException e){
                logger.error(e.getMessage());
            }
        }
    }


    public void updateDataPool(City<String, MyWeightedEdge> city){
        DataPool.setMyCity(city);
    }
}
