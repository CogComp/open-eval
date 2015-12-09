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


    public Configuration(String team_name, String description, String dataset, String task_variant, String evaluator, String configuration_id) {
        this.team_name = team_name;
        this.description = description;
        this.dataset = dataset;
        this.task_variant = task_variant;
        this.evaluator = evaluator;
        this.configuration_id = configuration_id;
    }

}