package models;

import java.util.List;

public class Configuration {

    public String configuration_name;
    public String description;
    public String dataset;
    public List<Record> records;

    public String task;
    public String task_variant;
    public String evaluator;
    public String configuration_id;


    public Configuration(String configuration_name, String description, String dataset, String task, String task_variant, String evaluator, String configuration_id) {
        this.configuration_name = configuration_name;
        this.description = description;
        this.dataset = dataset;
        this.task = task;
        this.task_variant = task_variant;
        this.evaluator = evaluator;
        this.configuration_id = configuration_id;
    }

}