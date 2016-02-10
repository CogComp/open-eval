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

    private static long counter = 0;

    public Record(String date, String comment, String repo,
                  String author, Metrics metrics) {
        this.date = date;
        this.comment = comment;
        this.repo = repo;
        this.author = author;
        this.record_id = "" + (counter++);
        this.metrics = metrics;
    }

    public Record(String date, String comment, String repo, String author) {
        this(date, comment, repo, author, new Metrics());
    }

    public Record() {
        this.record_id = "" + (counter++);
    }

}