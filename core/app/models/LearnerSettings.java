package models;

import java.util.List;

/**
 * Created by ryan on 3/13/16.
 */
public class LearnerSettings {
    public List<String> requiredViews;
    public String addedView;
    public int maxNumInstancesAccepted;
    public String error;

    public LearnerSettings(String error){
        this.error = error;
    }

    public LearnerSettings(String addedView, List<String> requiredViews, int maxNumInstancesAccepted){
        this.addedView = addedView;
        this.requiredViews = requiredViews;
        this.maxNumInstancesAccepted = maxNumInstancesAccepted;
        this.error = null;
    }
}
