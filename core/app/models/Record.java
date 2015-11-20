package models;

import java.util.List;

/**
 * Class to model a configuration
 */
public class Record {

    public String date;
    public String comment;
    public String repo;
    public String author;
    public double score;

    public String configuration_id;
    public String record_id;

    private static long counter = 0;;

    public Record(String date, String comment, String repo, String author, double score) {
        this.date = date;
        this.comment = comment;
        this.repo = repo;
        this.author = author;
        this.score = score;
        this.record_id = "" + (counter++);
    }

    public Record() {
        this.record_id = "" + (counter++);
    }

}