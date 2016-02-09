package models;

import java.util.List;

/**
 * Class to hold evaluation metrics of a record
 */
public class Metrics {

    public double precision;
    public double recall;
    public double f1;

    public int gold_count;
    public int correct_count;
    public int predicted_count;
    public int missed_count;
    public int extra_count;

    //public String metric_id;
    private static long counter = 0;
    
    public Metrics() {
        
    }
    
    public Metrics(
        double precision,
        double recall,
        double f1,

        int gold_count,
        int correct_count,
        int predicted_count,
        int missed_count,
        int extra_count
    ) {
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;

        this.gold_count = gold_count;
        this.correct_count = correct_count;
        this.predicted_count = predicted_count;
        this.missed_count = missed_count;
        this.extra_count = extra_count;
        //this.metric_id = "" + (counter++);
    }

}