package purchases.distribution.appl.Util;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

public class ArgsParser {

    public static final Logger logger = LoggerFactory.getLogger(ArgsParser.class);

    private HashMap<String,String> resources;
    private  String[] args;
    private Options options;


    public ArgsParser(String[] args){
        this.args = args;
        initializeOptions();
    }

    private void initializeOptions(){
        Option optionA = new Option("a", "agents", true, "Path to txt file with agents routes");
        Option optionC = new Option("c", "city",   true, "Path to gml file with city");

        this.options = new Options();
        this.options.addOption(optionA);
        this.options.addOption(optionC);
    }

    public void parseOptions() throws ParseException{
        CommandLineParser cmdLinePosixParser = new DefaultParser();
        CommandLine cmd = cmdLinePosixParser.parse(options, args);

        if(cmd.hasOption("a") && cmd.hasOption("c")) {
            this.resources = new HashMap<>();

            resources.put("city", cmd.getOptionValue("c"));
            resources.put("agents", cmd.getOptionValue("a"));

            DataPool.setAgentsFile(new File(resources.get("agents")));
            DataPool.setMyCityFile(new File(resources.get("city")));
        }
        else {
            logger.error("при запуске необходимо задать 2 аргумента");
        }
    }

    public HashMap<String, String> getResources() {
        return resources;
    }
}
