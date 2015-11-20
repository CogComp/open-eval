package models;

import java.util.List;


/**
 * Class to model a configuration
 */
public class Configuration {

    public String team_name;
    public String description;
    public String dataset;
    public List<Record> records;

    /*These should eventually not be strings, but im not positive how all of it is set up*/
    public String task_variant;
    public String evaluator;

    public String configuration_id;

    private static long counter = 0;;

    public Configuration(String team_name, String description, String dataset) {
        this.team_name = team_name;
        this.description = description;
        this.dataset = dataset;
        this.configuration_id = "" + (counter++);
    }

}