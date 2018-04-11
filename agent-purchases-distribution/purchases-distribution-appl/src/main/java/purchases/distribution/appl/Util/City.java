package purchases.distribution.appl.Util;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class City<V,E> extends SimpleWeightedGraph<V,E>{

    public static final Logger logger = LoggerFactory.getLogger(City.class);

    public City(Class<? extends E> edgeClass){
        super(edgeClass);
    }
}
