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
    public double score;

    public Metrics metrics;

    public String configuration_id;
    public String record_id;

    private static long counter = 0;

//<<<<<<< HEAD
    public Record(String record_id, String date, String comment, String repo, String author, double score) {
//=======
  //  public Record(String date, String comment, String repo,
    //              String author, double score, Metrics metrics) {
//>>>>>>> a7fe97d040d0f664689c0d56f026aca740a96332
        this.date = date;
        this.comment = comment;
        this.repo = repo;
        this.author = author;
        this.score = score;
//<<<<<<< HEAD
        this.record_id = record_id;
/*=======
        this.record_id = "" + (counter++);
        this.metrics = metrics;
    }

    public Record(String date, String comment, String repo, String author, double score) {
        this(date, comment, repo, author, score, new Metrics());
>>>>>>> a7fe97d040d0f664689c0d56f026aca740a96332
*/
    }
    
    public Record() {
        this.record_id = "" + (counter++);
    }

}