package models;

import java.util.List;

/**
 * Class to model a configuration
 *
 * Record will need a lot of work, and will possibly be different for different task variants
 */
public class Record {

    public String date;
    public String comment;
    public String repo;
    public String author;

    public Metrics metrics;

    public String configuration_id;
    public String record_id;
    public boolean isRunning;
    public String progressPageLink;

    public Record(String record_id, String date, String comment, String repo,
                  String author, Metrics metrics, String configuration_id) {
        this.date = date;
        this.comment = comment;
        this.repo = repo;
        this.author = author;
        this.record_id = record_id;
        this.metrics = metrics;
        this.configuration_id = configuration_id;
        this.isRunning = false;
        this.progressPageLink = "";
    }

    public Record(String record_id, String date, String comment, String repo,
                  String author, Metrics metrics) {
        this(record_id, date, comment, repo, author, metrics, null);
    }

    public Record(String record_id, String date, String comment, String repo, String author) {
        this(record_id, date, comment, repo, author, new Metrics(), null);
    }
    
    public Record() {
        
    }

    public void startRunning() {
        this.isRunning = true;
    }

    public void stopRunning() {
        this.isRunning = false;
    }
    
}